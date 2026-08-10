# Shopping List — Technical Design Document

**Version:** 1.0 (targets PRD v1.0, MVP scope frozen)
**Stack:** React Native (iOS/Android) + React (Web), shared TypeScript core
**Scope:** V1 only. V2 sync is *not* designed here; V1 satisfies the three obligations (UUIDs, `updated_at`, tombstones) and nothing more.
**Status:** Draft for review

---

## 0. How to read this document

The PRD is the authority on *what*. This document is the authority on *how*, and it defers to the PRD's conveyor-belt principle for any tie it does not explicitly break.

Where the PRD is silent and a decision had to be made anyway, the decision is marked **[D-n]** and collected in §16 so the PRD author can overrule it cheaply. Where the PRD asks for something the platform cannot deliver as literally written, it is marked **[C-n]** (conflict) and collected in §17.

---

## 1. Executive summary

### 1.1 The shape of the system

This is a local-first, single-user, offline-only application with two tables and no network calls. The engineering risk is therefore **not** in the data layer, the business logic, or the API — there is no API. It sits almost entirely in two places:

1. **Latency of the capture path.** AC#1 (≤3s from a locked phone, ≤3 interactions) and §6.2 (overlay up in ≤300ms) are the hardest requirements in the document, and they are hardest precisely on the surface the PRD calls "the highest-leverage feature in the product."
2. **Three-platform fidelity of one component.** §12 requires the externally-launched overlay to be "the same component" as the in-app one. It cannot be the same *code* on all three platforms (see §7), so it must be the same *specification*, verified by shared fixtures.

Everything else — sections, labels, undo, the 100-item trim — is a day of work each and is specified below mostly so nobody has to re-derive it.

### 1.2 The one decision that shapes everything

**The external capture surfaces are implemented natively and write directly to the shared SQLite database. They do not boot React Native.**

A cold RN start on a mid-tier Android device is 900ms–2s (Hermes, precompiled bytecode, no network). From a *locked* device, add unlock or keyguard-dismiss and the 3s budget in AC#1 is spent before the text field exists, and the 300ms budget in §6.2 is violated by a factor of five. The alternatives were:

| Option | Verdict |
|---|---|
| Boot RN from the tile, accept the latency | Fails §6.2 and probably AC#1. Rejected. |
| Keep the RN process warm via a foreground service / background mode | Fails on iOS outright; on Android it costs a persistent notification, which is a worse product than the one being built. Rejected. |
| **Native capture UI + a small duplicated write kernel** | **Chosen.** Costs ~250 LOC of Kotlin and ~250 LOC of Swift, and a real risk of behavioural drift, which §9.4 mitigates with shared golden fixtures. |

This is the expensive choice, and it is exactly the expense §12 anticipates when it says the capture surfaces "should receive the implementation budget freed by cutting drag-and-drop and multi-select."

---

## 2. Architecture

### 2.1 Layer diagram

```
┌───────────────────────────────────────────────────────────────────┐
│  PRESENTATION                                                     │
│                                                                   │
│  apps/mobile (React Native)      apps/web (React + Vite)          │
│  ├─ ListScreen                   ├─ ListScreen                    │
│  ├─ QuickAddOverlay              ├─ QuickAddOverlay               │
│  ├─ RecentlyCompletedScreen      ├─ RecentlyCompletedScreen       │
│  └─ SettingsScreen               └─ SettingsScreen                │
│         (separate view code, identical view-model contracts)      │
└───────────────────────────┬───────────────────────────────────────┘
                            │  hooks: useSections(), useQuickAdd(), …
┌───────────────────────────▼───────────────────────────────────────┐
│  packages/core  — pure TypeScript, zero platform imports          │
│  ├─ domain/      Item, Label, parseCapture(), resolveSticky()     │
│  ├─ usecases/    captureItems, completeItem, undoComplete,        │
│  │               mergeLabels, trimCompleted, …                    │
│  └─ ports/       ItemRepository, LabelRepository, Clock, IdGen    │
└───────────────────────────┬───────────────────────────────────────┘
┌───────────────────────────▼───────────────────────────────────────┐
│  packages/data  — SQL, migrations, repository implementations     │
│  └─ SqliteDriver (interface)                                      │
└──────────┬─────────────────────┬──────────────────┬───────────────┘
           │                     │                  │
┌──────────▼──────────┐ ┌────────▼────────┐ ┌───────▼──────────────┐
│ expo-sqlite (iOS/   │ │ sqlite-wasm     │ │  NATIVE CAPTURE      │
│ Android, app proc.) │ │ (OPFS, worker)  │ │  KERNEL              │
└──────────┬──────────┘ └────────┬────────┘ │  Kotlin / Swift      │
           │                     │          │  same .db file       │
      shopping.db           shopping.db     └───────┬──────────────┘
      (app group /               (OPFS)             │
       app files dir) ◄──────────────────────────────┘
```

### 2.2 Why the UI is not shared

The instinct is `react-native-web` and one `QuickAddOverlay`. Rejected, for reasons that are specific to this product rather than general:

- The whole UX rests on **keyboard behaviour** — overlay anchored above the keyboard, autofocus, stay-open-on-submit, "done" key handling. RNW maps this onto DOM inputs with `position: fixed`, where the visual viewport moves instead of the layout viewport. The two need genuinely different code, and it would end up as `Platform.OS === 'web'` branches inside the most important component in the product.
- Sticky section headers are `position: sticky` on web (free, GPU-composited) and a `SectionList` prop on native. RNW's implementation of the latter is the weaker of the two.
- The web target is a PWA whose job (§12, P1) is "installable, app shortcut to Quick Add." It is the *least* demanding of the three surfaces and does not justify constraining the other two.

What *is* shared is everything below the view: parsing, label resolution, ordering, trim policy, undo semantics, SQL. That is where bugs would actually be expensive. View code is ~30% of the codebase and the cheapest part to write twice.

### 2.3 Monorepo layout

```
shopping-list/
├─ apps/
│  ├─ mobile/                 Expo (prebuild / dev client — not Expo Go)
│  │  ├─ src/
│  │  ├─ android/             TileService, CaptureActivity, CaptureDao
│  │  ├─ ios/                 ControlWidget, AppIntents, CaptureScene
│  │  └─ plugins/             Expo config plugins for the above
│  └─ web/                    Vite + React + vite-plugin-pwa
├─ packages/
│  ├─ core/                   domain + usecases + ports (no I/O)
│  ├─ data/                   SQL, migrations, repositories, drivers
│  ├─ tokens/                 design tokens (§11), emitted as TS + CSS vars
│  └─ fixtures/               golden parser/ordering fixtures (§9.4)
└─ tools/                     eslint config, tsconfig base, turbo pipeline
```

pnpm workspaces + Turborepo. `packages/core` has an ESLint rule banning imports from `react`, `react-native`, and `node:*` — it must stay runnable inside a plain Jest process and, later, inside anything else.

**Expo prebuild, not Expo Go.** A Quick Settings tile and a Control Center control are native targets; managed Expo cannot express them. Config plugins keep the native edits reproducible rather than hand-patched.

---

## 3. Data layer

### 3.1 Schema (SQLite, canonical DDL)

```sql
-- migration 001_init.sql

PRAGMA journal_mode = WAL;
PRAGMA foreign_keys = ON;

CREATE TABLE labels (
  id           TEXT    PRIMARY KEY NOT NULL,   -- UUIDv7, client-generated
  name         TEXT    NOT NULL,               -- display form, without '@'
  name_folded  TEXT    NOT NULL,               -- NFC + full casefold, for matching
  color_index  INTEGER NOT NULL,
  created_at   INTEGER NOT NULL,               -- epoch ms, UTC
  updated_at   INTEGER NOT NULL,
  last_used_at INTEGER NOT NULL,
  deleted_at   INTEGER
);

CREATE UNIQUE INDEX ux_labels_folded
  ON labels(name_folded)
  WHERE deleted_at IS NULL;

CREATE TABLE items (
  id           TEXT    PRIMARY KEY NOT NULL,   -- UUIDv7, client-generated
  title        TEXT    NOT NULL,
  label_id     TEXT    REFERENCES labels(id) ON DELETE RESTRICT,
  state        TEXT    NOT NULL CHECK (state IN ('active','completed')),
  created_at   INTEGER NOT NULL,
  updated_at   INTEGER NOT NULL,
  completed_at INTEGER,
  deleted_at   INTEGER,
  CHECK ((state = 'completed') = (completed_at IS NOT NULL))
);

-- List screen: active items grouped by label, ordered by created_at.
CREATE INDEX ix_items_active
  ON items(label_id, created_at, id)
  WHERE deleted_at IS NULL AND state = 'active';

-- Recently Completed: reverse-chronological, and the trim scan.
CREATE INDEX ix_items_completed
  ON items(completed_at DESC)
  WHERE deleted_at IS NULL AND state = 'completed';

-- Section ordering.
CREATE INDEX ix_labels_recent
  ON labels(last_used_at DESC)
  WHERE deleted_at IS NULL;
```

Notes on choices that are not obvious:

- **`ON DELETE RESTRICT`, deliberately.** There are no hard deletes (§5.1 of the PRD); the constraint exists to make a hard delete fail loudly in development rather than silently orphan rows.
- **Partial indexes** everywhere. Every read filters `deleted_at IS NULL`, so tombstones should not be in the hot index. On a list of a few hundred rows this is irrelevant to performance and entirely relevant to making the query plans legible.
- **`name_folded` as a stored column, not `COLLATE NOCASE`.** SQLite's `NOCASE` is ASCII-only. `@Café` and `@CAFÉ` must be the same label. Folding is done in TypeScript (`.normalize('NFC').toLocaleLowerCase()`) and — critically — in Kotlin and Swift by the capture kernel, using the same rule (§7.4).
- **The `CHECK` on `completed_at`** makes the undo path (§6.3) unable to leave a half-reverted row behind.

### 3.2 IDs: UUIDv7, and why it earns the ordering guarantee

The PRD requires client-generated UUIDs. Use **v7** rather than v4, because it solves a bug that v4 would leave open.

§7.2 orders items by `created_at` ascending. AC#3 requires a pasted six-line block to create six items *in order*. Those six inserts happen in one transaction, within the same millisecond. `created_at` alone does not order them, and the tiebreaker with a v4 UUID is random — the six items would appear in a shuffled order roughly 719 times out of 720.

UUIDv7 embeds a millisecond timestamp followed by a monotonic counter, so:

```sql
ORDER BY created_at ASC, id ASC
```

is a total order that matches insertion order, with no extra column. The generator must implement the monotonic-counter variant (RFC 9562 §6.2, "fixed-length dedicated counter"), not naive random-per-call.

Same generator contract in all three languages. `packages/fixtures/uuidv7.json` asserts that 1000 IDs generated in a tight loop are strictly increasing as strings.

### 3.3 Storage per platform

| Platform | Driver | Location |
|---|---|---|
| iOS | `expo-sqlite` (app), `SQLite3`/GRDB (kernel) | **App Group container**, `group.$(BUNDLE)/shopping.db` |
| Android | `expo-sqlite` (app), `SQLiteOpenHelper` (kernel) | App-private files dir, `shopping.db` |
| Web | `@sqlite.org/sqlite-wasm`, `opfs-sahpool` VFS, in a dedicated Worker | OPFS |

**iOS file protection.** The database file and its `-wal`/`-shm` siblings must be set to `NSFileProtectionCompleteUntilFirstUserAuthentication`. The default (`CompleteUntilFirstUserAuthentication` for most app files, but `Complete` if the target opts in) will make writes from a Lock Screen widget fail with `EPERM` after a reboot. §12 explicitly asks for "usable without unlocking to the full app." This is a two-line entitlement change and a very confusing three-day bug if missed.

**Android multi-process.** Keep `CaptureActivity` in the **main process** (no `android:process` attribute). Same process means one `SQLiteOpenHelper` connection pool and no cross-process locking. Because the RN instance may or may not be alive, the kernel must not assume it; it talks to the DAO directly and posts a local broadcast (§7.2) that the RN side listens for *if* it exists.

**Web multi-tab.** `opfs-sahpool` gives one writer per origin. Elect it with the Web Locks API (`navigator.locks.request('db-writer', …)`); non-holders proxy writes through a `BroadcastChannel` to the holder and receive invalidation events back. If OPFS is unavailable (older Safari, some private-browsing modes), fall back to the `kvvfs` VFS backed by `localStorage` — adequate for a database that will not exceed a few hundred KB — and surface nothing to the user. **[D-1]**

### 3.4 Migrations

`user_version` pragma, forward-only, applied in a transaction at startup before the first read. `packages/data/migrations/` holds numbered `.sql` files; a test asserts that applying all migrations to an empty DB produces a schema byte-identical to `schema.sql`. The native kernels do **not** run migrations — they open the DB read/write and, if `user_version` is lower than they expect, they refuse to write and fall back to opening the full app. **[D-2]** This keeps migration logic in exactly one place at the cost of one degraded launch after an update.

### 3.5 `updated_at` enforcement

The PRD calls this non-negotiable, and the failure mode (one repository method forgets, and V2 sync silently loses that field forever) is invisible until it is expensive. Two guards:

1. All writes go through `packages/data/repositories/*`, which set `updated_at` from the injected `Clock`. No raw SQL in `usecases` or UI.
2. A **debug-build-only** trigger asserts it:

```sql
CREATE TRIGGER dbg_items_updated_at BEFORE UPDATE ON items
WHEN NEW.updated_at <= OLD.updated_at
BEGIN
  SELECT RAISE(ABORT, 'updated_at not bumped');
END;
```

Shipped in debug/test builds and in CI, omitted from release (where a hard abort is a worse outcome than a stale timestamp).

---

## 4. Domain core

### 4.1 The capture parser

This is the single most-replicated piece of logic in the system (TS, Kotlin, Swift) and therefore gets the most precise specification.

```
parseCapture(input: string, sticky: LabelRef) -> { lines: ParsedLine[], sticky: LabelRef }

1. Normalise: replace \r\n and \r with \n. NFC-normalise.
2. Split on \n.
3. For each line, in order:
   a. Trim leading/trailing Unicode whitespace.
   b. If empty → skip (contributes no item, does not change sticky).
   c. Extract the FIRST label token:
        - a token is '@' that is at the start of the line or preceded by
          whitespace, followed by one or more non-whitespace, non-'@' chars;
        - trailing punctuation from the set  . , ; : ! ?  is stripped off
          the token and returned to the title.
   d. If a token was found:
        - remove it from the line; collapse the resulting double space;
          re-trim;
        - resolve/create the label (§4.2); this becomes the new sticky.
   e. Title := the remaining text.
   f. If title is empty:
        - if a token was found → line yields NO item, but sticky is updated;
        - else → no-op.
   g. Otherwise emit { title, labelId: sticky }.
4. Return the lines and the final sticky value.
```

Four things here are decisions the PRD does not make:

- **[D-3] `@` must be at a word boundary.** Without this, `order from shop@costco.com` creates a label named `costco.com`. The PRD's §6.4 says "the first `@token`", which read literally would do exactly that. Word-boundary anchoring is what a user means.
- **[D-4] Trailing punctuation is stripped.** `get milk @costco.` should not create the label `costco.`. Note that this interacts with D-3: interior dots survive (`@trader-joes.local` is a label named `trader-joes.local`), only trailing ones are shed.
- **[D-5] A line containing only a token sets sticky and creates no item.** So pasting `@costco` / `milk` / `eggs` tags all three lines, which is the obviously-intended reading, and matches §6.4's "lines without a token use the sticky selection."
- **[D-6] Sticky updates progressively through a multi-line submit and persists after it.** §6.4 says the extracted token becomes the new sticky; §6.2 says sticky survives submit. Combined: after submitting the block above, the chip row shows `@costco` selected.

Every one of these is encoded in `packages/fixtures/parser.json` (§9.4), which is the artifact the Kotlin and Swift kernels are tested against.

### 4.2 Label resolution

```ts
resolveLabel(token: string): LabelId
  folded = token.normalize('NFC').toLocaleLowerCase()
  existing = SELECT id FROM labels WHERE name_folded = ? AND deleted_at IS NULL
  if existing → return it            // display name unchanged; @COSTCO hits @costco
  else → INSERT with name = token as typed, color_index = nextColor()
```

`nextColor()` implements §8.2: lowest palette index not currently held by a live label; if all ten are held, `count(live labels) mod 10`. Computed inside the same transaction as the insert to avoid two labels created in one paste sharing a colour.

Note that `resolveLabel` **writes**. The parser is therefore pure and returns tokens; the *use case* resolves them. This keeps `packages/core/domain` free of I/O and makes the parser fixtures runnable with no database.

### 4.3 Use cases

| Use case | Transaction contents |
|---|---|
| `captureItems(input, sticky)` | resolve/create labels → insert N items → bump `last_used_at` on each touched label. One transaction. Partial failure is not a state the user can observe. |
| `completeItem(id)` | `state='completed'`, `completed_at=now`, `updated_at=now` → then `trimCompleted()` (§4.5) |
| `undoComplete(id)` | `state='active'`, `completed_at=NULL`, `updated_at=now` |
| `editTitle(id, newTitle)` | re-run token extraction on the new title (§7.4 of PRD) → possibly resolve/create a label → update `title`, `label_id`, `updated_at`. `created_at` untouched, so position is preserved (PRD §7.2). |
| `renameLabel(id, name)` | update `name`, `name_folded`, `updated_at`. Items untouched. Rejects on folded collision with a live label. |
| `mergeLabels(source, target)` | `UPDATE items SET label_id=target, updated_at=now WHERE label_id=source` → `UPDATE labels SET deleted_at=now WHERE id=source`. **One transaction.** V2 note: this shape (reassign + tombstone, never rename) is what PRD §V2.2 requires; it costs nothing to get right now. |
| `deleteLabel(id)` | `UPDATE items SET label_id=NULL, updated_at=now WHERE label_id=?` → tombstone label. Items survive, move to Inbox. |
| `readdCompleted(id)` | insert a **new** item (new UUID, new `created_at`) with the same title and `label_id`; the completed row is untouched (PRD §9: "The entry remains in Recently Completed"). Bumps the label's `last_used_at`. |

### 4.4 Reads

Two queries drive the entire List screen.

```sql
-- Sections, in render order. Inbox first, then by last_used_at desc.
SELECT NULL AS label_id, NULL AS name, NULL AS color_index, 0 AS ord
UNION ALL
SELECT l.id, l.name, l.color_index, 1
FROM labels l
WHERE l.deleted_at IS NULL
  AND EXISTS (SELECT 1 FROM items i
              WHERE i.label_id = l.id
                AND i.deleted_at IS NULL
                AND i.state = 'active')
ORDER BY ord, last_used_at DESC;
```

Inbox is emitted unconditionally (PRD §7.3: "always pinned first, even when empty"); every other section requires at least one active item.

```sql
-- All active items, one pass, already in render order.
SELECT id, title, label_id, created_at
FROM items
WHERE deleted_at IS NULL AND state = 'active'
ORDER BY (label_id IS NOT NULL), created_at ASC, id ASC;
```

Grouping happens in TS after a single scan. At the realistic ceiling (~300 active items) this is sub-millisecond; there is no reason to issue one query per section.

```sql
-- Recently Completed
SELECT id, title, label_id, completed_at
FROM items
WHERE deleted_at IS NULL AND state = 'completed'
ORDER BY completed_at DESC
LIMIT 100;
```

### 4.5 The 100-item trim

PRD §9: opportunistic, on write, no scheduler.

```sql
UPDATE items SET deleted_at = :now, updated_at = :now
WHERE id IN (
  SELECT id FROM items
  WHERE deleted_at IS NULL AND state = 'completed'
  ORDER BY completed_at DESC
  LIMIT -1 OFFSET 100
);
```

Runs inside the `completeItem` transaction. `LIMIT -1 OFFSET 100` is SQLite's "everything after the hundredth."

It also runs after `undoComplete` — not because undo can push anything over the cap (it removes a completed row, it cannot add one), but because it makes the invariant "after any state transition, at most 100 live completed rows exist" unconditionally true, which is one fewer thing to reason about.

**Interaction with undo (worth stating explicitly):** trimming cannot tombstone the item currently sitting in the undo window, because that item is by construction the *most recent* completion — rank 1, never rank 101.

---

## 5. State management and reactivity

### 5.1 Model

```
UI ──dispatch──▶ use case ──▶ repository ──▶ SQLite
                                              │
                                        DbChangeBus  (topic: 'items' | 'labels')
                                              │
UI ◀── re-query ◀── TanStack Query invalidate ┘
```

- **TanStack Query** for caching and deduplication, with a synchronous-ish `queryFn` hitting SQLite. No optimistic-update machinery: the write *is* local and completes in under a millisecond, so the honest render path is fast enough. (This is a genuine benefit of local-first that teams routinely forget and then build optimistic layers anyway.)
- Writes publish a topic to `DbChangeBus`; a thin adapter maps topics to `queryClient.invalidateQueries`. On web, the bus is a `BroadcastChannel` so a second tab stays live. On Android, the native kernel's local broadcast is translated into a bus event by a small native module.

### 5.2 Ephemeral UI state

Zustand, one store, three slices, none of it persisted:

- `quickAdd`: `{ open, text, stickyLabelId, autocomplete: { query, results, highlighted } }`. **`stickyLabelId` resets to `null` (Inbox) on close** (PRD §6.2), which means it lives here and never in SQLite or async storage.
- `undo`: `{ itemId, expiresAt } | null`, depth 1 (§6.3).
- `editing`: `{ itemId, selection } | null`.

Theme (System/Light/Dark) is the only persisted preference; `AsyncStorage` / `localStorage`, not the database.

---

## 6. Screen-level design

### 6.1 Quick Add overlay

The component contract, identical on all three platforms:

```ts
interface QuickAddProps {
  origin: 'fab' | 'external';   // affects dismissal only, never behaviour
  onDismiss(): void;
}
```

**Native (React Native).**

- Rendered as a `react-native-screens` **transparent modal route**, not RN's `<Modal>`. `<Modal>` on Android creates a separate window whose `windowSoftInputMode` is not inherited, which produces the classic "keyboard covers the input" bug and, worse, an inconsistent animation on open.
- `react-native-keyboard-controller` for `KeyboardAvoidingView` + `useKeyboardHandler`. It reports keyboard frames on both platforms via the same interpolation, which matters because the overlay must be *anchored to the keyboard*, not merely pushed above it.
- **Prewarm the keyboard.** The 300ms budget is dominated by keyboard animation, not by React. The FAB's `onPressIn` (not `onPress`) mounts the overlay route; `autoFocus` on the `TextInput` starts the IME immediately. On Android also set `android:windowSoftInputMode="adjustResize|stateVisible"` on the host activity.
- **Submit** binds to `onSubmitEditing` with `blurOnSubmit={false}` and `submitBehavior="submit"`. This is the load-bearing prop for "overlay stays open, keyboard stays up." Multiline inputs default to inserting a newline; the requirement is that the *return key commits* while pasted newlines still split. Hence: `multiline` is **false**, the input grows by measuring content, and pasted text containing `\n` is handled in `onChangeText` — paste is the only way a newline enters the buffer. **[D-7]**

**Web.**

- A fixed-position bar bound to `visualViewport` (`resize` + `scroll` listeners) so it tracks the software keyboard on mobile browsers, with a `@media (pointer: fine)` branch that centres it as a dialog on desktop.
- `<input>`, not `<textarea>`, for symmetry with D-7. `Enter` submits; `paste` is intercepted.
- Dismissal: `Escape`, backdrop click, and `popstate` (so Android Chrome's back gesture closes the overlay rather than leaving the PWA). Push a history entry on open.

**Label chip row.** Horizontal `ScrollView` / flex row, `ORDER BY last_used_at DESC`, leading Inbox chip. At ten labels this is a fixed-size list; no virtualisation.

**Autocomplete (§6.4).** Triggered when the character before the caret is `@` at a word boundary. Queries `SELECT … WHERE name_folded LIKE ? || '%'` — prefix-only, capped at 6 results, rendered above the input. `Enter` completes the highlighted suggestion *instead of* submitting when the dropdown is open. **[D-8]** — the PRD assigns `Enter` two jobs and does not say which wins; completion winning is the standard and the more forgiving of the two, because a second `Enter` still submits.

### 6.2 List screen

- **Native:** `@shopify/flash-list` v2 in sectioned mode with `stickyHeaderIndices`. `SectionList` would also work at this data size; FlashList is chosen for recycling behaviour during the row-exit animation, which is where a naive list drops frames.
- **Web:** plain DOM, no virtualisation (a few hundred rows), `position: sticky` headers.

**Row rendering and the URL requirement (§7.1).** Two modes on one row:

1. *Display mode* — the title is linkified with a conservative URL regex (scheme-required, plus bare `www.`), split into `<Text>` / `<a>` segments. Link segments get their own press handler; the rest of the row's press handler enters edit mode.
2. *Edit mode* — the whole title becomes a `TextInput` / `<input>` with the same font metrics and padding, so there is no visible shift on transition. Commit on blur or Enter.

**Caret placement at the tap position** is the fiddly part. `TextInput` cannot be told "put the caret where the finger landed" directly. Approach: capture `nativeEvent.locationX/locationY` from the press, then on mount set `selection` to an index computed from a text-measurement pass (`@shopify/react-native-skia`'s paragraph API is overkill; use `Text`'s `onTextLayout` lines, which give per-line frames, plus a binary search over `measureText` on the tapped line). On web this is free — `document.caretPositionFromPoint`. **[C-1]** — the native implementation carries real risk; the agreed fallback if it proves flaky on a given platform is caret-at-end, which is what most notes apps actually do.

**Long press: nothing.** Explicitly set `onLongPress={undefined}` and `delayLongPress` unset; on web, `user-select: none` and `-webkit-touch-callout: none` to suppress the browser's own selection menu, which is the platform's default long-press behaviour and would violate §7.4 by accident.

### 6.3 Undo

```
tap checkbox
  → completeItem() runs immediately (a real write, not a deferred one)
  → row exit animation (150ms, height + opacity)
  → toast: "Completed · UNDO", 4000ms
  → on UNDO: undoComplete(); row re-enters at its original position
```

The write is not deferred. This matters: if the user backgrounds the app, kills it, or the tile writes concurrently during the 4-second window, a deferred write is lost and the item is neither completed nor visibly pending. Committing immediately and reversing on demand has no such hole, and reversal is exact because position derives from `created_at` (§3.2), which the round trip never touches.

**Undo depth is 1. [D-9]** A second completion within the window dismisses the first toast and commits it. Rationale: a stack of toasts is the "filing cabinet" failure mode, and PRD §13 already argues that five taps beats a multi-select flow — the user completing five items rapidly is doing the expected thing, not something that needs five levels of regret.

`AccessibilityInfo.announceForAccessibility` fires on completion, because the row silently vanishing is otherwise unannounced to a screen reader.

### 6.4 Recently Completed and Settings

Straightforward. Two notes:

- Relative timestamps ("2h ago") use `Intl.RelativeTimeFormat` and re-render on a 60s interval only while the screen is focused.
- **Merge** (§8.3) is the destructive one and gets the confirmation the PRD assigns to Delete; the PRD asks for confirm on Delete only, but merge is equally irreversible and mis-tappable in a list of similar names. **[D-10]** — flagging rather than assuming; happy to drop it.

---

## 7. External capture surfaces

This is where the budget goes.

### 7.1 The capture kernel contract

Each native surface needs exactly one operation:

```
capture(rawText: String, stickyLabelId: UUID?) -> Unit
```

implemented as: parse (§4.1) → resolve/create labels (§4.2) → insert items → bump `last_used_at`, in one transaction, against the shared `shopping.db`. Approximately 250 lines in each of Kotlin and Swift. **Nothing else is duplicated** — no reads beyond the label list for the chip row, no editing, no completion, no settings.

The kernel is the only place where behavioural drift between platforms is possible, and §9.4's shared fixtures are the entire mitigation. Treat a fixture failure as a release blocker.

### 7.2 Android — Quick Settings tile (P0)

```
TileService.onClick()
  → startActivityAndCollapse(CaptureActivity)   // API 34: PendingIntent variant
CaptureActivity
  → Theme.Translucent.NoTitleBar, showWhenLocked=true, turnScreenOn=true
  → Jetpack Compose: TextField (autofocus) + LazyRow of label chips
  → submit → CaptureKernel.capture() → keeps activity open, clears field
  → back / outside tap → finish()
  → on finish, if the RN app is alive: LocalBroadcast("db-changed")
```

Interaction budget for AC#1, from a locked phone: swipe down (1) → tap tile (2) → type → tap send (3). Three interactions. Cold Activity launch with Compose is ~200–400ms; typing "milk" is ~1s; total comfortably under 3s.

`showWhenLocked` is what makes "usable without unlocking" true. Note that `startActivityAndCollapse(Intent)` is deprecated on API 34+ in favour of the `PendingIntent` overload — use both behind a version check.

**Share target** (bonus, near-free): an `ACTION_SEND` intent filter on `CaptureActivity` prefills the text. Not in the PRD; not built unless asked. **[D-11]**

### 7.3 iOS — Control Center control (P0)

**[C-2] This is the requirement the platform fights hardest.**

iOS 18 Control Center controls are built with `ControlWidget` and can be exactly three things: a button, a toggle, or a value control. **They cannot host a text field.** There is no API by which arbitrary UI, let alone a keyboard, appears over Control Center. The literal reading of §12 — "Control Center control → Quick Add overlay," same component, ≤300ms, without unlocking — is not implementable on iOS as of iOS 18/26.

The closest achievable behaviour:

```
ControlWidgetButton → AppIntent(openAppWhenRun: true)
  → app launches with a deep link  shoppinglist://quickadd
  → UIWindowScene shows a NATIVE SwiftUI capture sheet IMMEDIATELY,
    before/independently of the RN root view
  → RN bridge initialises in the background, behind the sheet
  → submit → CaptureKernel.capture() (Swift, App Group DB)
  → dismiss → RN root is by then warm; the List screen is already correct
```

The user experiences: tap control → (device unlock, if locked) → keyboard up. The measurable gap versus the PRD is the unlock step, which iOS does not let an app skip for a UI that accepts text input.

The native sheet is what preserves the *latency* requirement even though it cannot preserve the *no-unlock* one: it renders in ~150ms after scene attach, whereas waiting for RN would be 1–2s.

**Complementary surfaces that dodge the constraint** (both P1 in the PRD, both worth pulling forward for this reason):

- **Shortcuts / Siri App Intent with a `String` parameter.** "Hey Siri, add milk to my shopping list" runs entirely without unlocking and without any UI at all — zero interactions, well under 3s. This is arguably a *better* answer to AC#1 than the Control Center path.
- **Lock Screen widget** → same deep link as above.
- **Action Button** → the same App Intent.

### 7.4 Web — PWA (P1)

- `vite-plugin-pwa`, manifest `shortcuts` entry pointing at `/?compose=1`, which opens the overlay on load.
- `launch_handler: { client_mode: "navigate-existing" }` so the shortcut focuses an existing window rather than opening a second one (which would then contend for the OPFS write lock).
- Web Share Target (`method: POST`, `enctype: multipart/form-data`) so Android share-sheet text lands in the overlay. Same **[D-11]** caveat.
- Service worker caches the shell only. There is no network data, so there is no data-caching strategy to design — a pleasant consequence of local-first.

### 7.5 Concurrency

Two writers can touch the DB at once (kernel + app on mobile; two tabs on web). WAL plus `PRAGMA busy_timeout = 3000` handles it: writes are single-row and sub-millisecond, contention is effectively nil, and the timeout covers the pathological case rather than being load-bearing.

---

## 8. Performance budgets

Derived from PRD §6.2 and §14, measured on a Pixel 6a and an iPhone 12 (P75 targets).

| Path | Budget | Where it goes |
|---|---|---|
| FAB tap → input focused, keyboard up | **300ms** | route push 30ms · layout 20ms · keyboard animation 250ms |
| Tile tap → input focused (Android) | **600ms** | activity launch 300ms · Compose first frame 100ms · IME 250ms |
| Control tap → input focused (iOS, unlocked) | **900ms** | scene attach 400ms · SwiftUI sheet 150ms · IME 300ms |
| Submit → input cleared | **50ms** | parse + N inserts + invalidate + re-render |
| App cold start → List interactive | **1.5s** | Hermes + bytecode precompilation, no network |
| Checkbox tap → row gone | **150ms** | exit animation; the write is already committed |

Enforcement: a Maestro flow with `startRecording`/frame-timing on Android and a `signpost`-instrumented XCTest on iOS, both run in CI on every merge to `main`, both failing the build on regression. Budgets in this table are test constants, not documentation.

Bundle/runtime hygiene: Hermes on, RAM bundles off (single small bundle), `react-native-screens` enabled, no `moment`, no `lodash` barrel imports, `console.*` stripped in release.

---

## 9. Testing

### 9.1 Unit (Jest, `packages/core`)

Parser, colour allocation, ordering comparators, trim selection, sticky-label state machine. No mocks — these functions are pure. Target: 100% branch coverage on `parseCapture`, which is non-negotiable given that two other languages must match it.

### 9.2 Integration (`packages/data`)

`better-sqlite3` in-memory, real migrations, real SQL. Covers: merge atomicity, delete-label-preserves-items, the trim boundary at exactly 100/101, undo restoring position, `updated_at` bumped on every mutation (a reflective test that enumerates every repository method and asserts the timestamp moved).

### 9.3 E2E

- Mobile: **Maestro** (chosen over Detox for the tile/Control Center flows, which need to leave the app).
- Web: **Playwright**, including a two-tab test for the write-lock election.
- Each of the seven acceptance criteria in PRD §14 maps to exactly one named E2E test (§10).

### 9.4 Cross-language golden fixtures — the important one

`packages/fixtures/parser.json`:

```json
[
  { "name": "multiline splits",
    "input": "carrots\ntomatoes\nonions", "sticky": null,
    "expect": { "items": [
      {"title":"carrots","label":null},
      {"title":"tomatoes","label":null},
      {"title":"onions","label":null}], "sticky": null } },

  { "name": "commas are not separators",
    "input": "carrots, 2lb organic bag", "sticky": null,
    "expect": { "items": [{"title":"carrots, 2lb organic bag","label":null}],
                "sticky": null } },

  { "name": "token tags only its own line",
    "input": "milk @costco\neggs", "sticky": null,
    "expect": { "items": [
      {"title":"milk","label":"costco"},
      {"title":"eggs","label":"costco"}], "sticky": "costco" } },

  { "name": "second token stays literal",
    "input": "milk @costco @qfc", "sticky": null,
    "expect": { "items": [{"title":"milk @qfc","label":"costco"}],
                "sticky": "costco" } },

  { "name": "email is not a token",           /* D-3 */
    "input": "order from shop@costco.com", "sticky": null,
    "expect": { "items": [{"title":"order from shop@costco.com","label":null}],
                "sticky": null } },

  { "name": "trailing period stripped",       /* D-4 */
    "input": "milk @costco.", "sticky": null,
    "expect": { "items": [{"title":"milk","label":"costco"}],
                "sticky": "costco" } },

  { "name": "bare token line sets sticky",    /* D-5 */
    "input": "@costco\nmilk\neggs", "sticky": null,
    "expect": { "items": [
      {"title":"milk","label":"costco"},
      {"title":"eggs","label":"costco"}], "sticky": "costco" } },

  { "name": "unicode case folding",
    "input": "pastries @CAFÉ", "sticky": null, "existingLabels": ["café"],
    "expect": { "items": [{"title":"pastries","label":"café"}],
                "sticky": "café" } }
]
```

This file is consumed by three test suites: Jest, JUnit (Kotlin kernel), XCTest (Swift kernel). It is the contract that makes "the same component" in PRD §12 true in the only sense that is achievable — same observable behaviour, three implementations.

---

## 10. Acceptance criteria → implementation → test

| # | Criterion | Implementation | Test |
|---|---|---|---|
| 1 | ≤3s, ≤3 interactions from locked phone | §7.2 (Android: fully met) · §7.3 (iOS: met except unlock, **[C-2]**) | `e2e/capture_from_locked.yaml`, perf-gated |
| 2 | Six items to `@costco`, tag typed once | Sticky label §5.2 + §6.1; D-6 | `e2e/sticky_label_run.yaml` |
| 3 | Six-line paste → six ordered items | §4.1 parser + §3.2 UUIDv7 total order | `parser.json` + `data/ordering.test.ts` |
| 4 | One tap, reversible 4s, never confirms | §6.3 | `e2e/complete_and_undo.yaml` |
| 5 | Zero-config first launch | Migrations at startup, no onboarding route, no auth | `e2e/fresh_install.yaml` |
| 6 | Every screen ≤2 taps from List | §4 IA: hamburger → screen | Static route-graph assertion in CI |
| 7 | No feature not in the PRD | — | PR review checklist; §13 cut-list is quoted in `CONTRIBUTING.md` |

---

## 11. Design tokens and accessibility

`packages/tokens` emits TypeScript objects and CSS custom properties from one source, so the RN and web UIs cannot drift on colour or spacing.

```
color.bg          #FFFFFF / #000000
color.text        #000000 / #FFFFFF
color.textMuted   #8E8E93 / #8E8E93
color.label[0..9] fixed palette, the only chromatic values in the app
size.item         17pt / 400
size.header       13pt / 600, uppercase tracking
space.row         12pt vertical, 16pt horizontal
tap.min           44pt   ← enforced by hitSlop, not by row height
```

- `tap.min` is achieved with `hitSlop` on the checkbox rather than by inflating the row, preserving §11's compact density.
- Dynamic Type / browser font scaling is honoured up to 200%; beyond that, `allowFontScaling` caps to keep one-line rows. Row height grows; it never wraps to two lines. **[D-12]**
- The checkbox exposes `accessibilityRole="checkbox"` with the item title as its label; the section header is `accessibilityRole="header"`. Colour dots carry no information not also present in the header text (§8.2 calls colour "a scanning aid"), so contrast requirements apply only to text.
- Reduced-motion: row exit becomes an instant removal; overlay fades without translation.

---

## 12. Observability and error handling

Deliberately thin, matching a product with no server:

- **Crash reporting only** (Sentry or equivalent), no analytics events in v1. There is no funnel to optimise and no A/B test to run; adding an events pipeline would be a §13-style quiet return of scope.
- Database open failure is the one unrecoverable error: show a single-screen message with a "reset local data" action rather than a crash loop. Rare, but the alternative is an app that cannot be un-bricked without reinstalling.
- Failed writes from the native kernel (locked file protection, migration mismatch per D-2) fall back to opening the full app with the typed text preserved in the intent/deep link. Never silently drop user text.

---

## 13. Build, release, CI

| Stage | Contents |
|---|---|
| PR | typecheck · lint (incl. the `core`-purity rule) · Jest · `packages/data` integration · fixture parity (Jest + JUnit + XCTest) |
| main | above + Maestro (Android emulator, iOS sim) + Playwright + perf gates from §8 |
| release | EAS Build (mobile), Vite build + Netlify/Cloudflare (web) |

Native code lives in `apps/mobile/android|ios` and is committed (prebuild output is checked in) so that kernel changes are reviewable diffs rather than regenerated artifacts.

---

## 14. Risks

| Risk | Severity | Mitigation |
|---|---|---|
| iOS Control Center cannot host text input (**C-2**) | High — it is a P0 in the PRD | Native sheet on deep link (§7.3) + pull Siri/Shortcuts intent forward from P1; needs a PRD decision |
| Parser drift between TS/Kotlin/Swift | High — silently wrong tagging | Golden fixtures as a release blocker (§9.4) |
| Caret-at-tap-position on native (**C-1**) | Medium | Timeboxed; documented fallback is caret-at-end |
| iOS file protection blocks pre-unlock writes | Medium — fails silently after reboot | Entitlement set in the config plugin; an E2E test that reboots the simulator |
| OPFS unavailable in some browsers | Low | `kvvfs` fallback (D-1); DB is tiny |
| RN keyboard behaviour regressions across versions | Medium — it is the core interaction | Pin RN minor; keyboard behaviour covered by Maestro, not by manual QA |

---

## 15. Milestones

| M | Deliverable | Notes |
|---|---|---|
| M0 | Monorepo, tokens, migrations, empty app on 3 platforms | 1 week |
| M1 | `packages/core` + `packages/data` complete, fully unit-tested | Parser and fixtures land here, before any UI |
| M2 | List screen: sections, ordering, complete, undo, inline edit | The aesthetic is locked at the end of this milestone |
| M3 | Quick Add overlay (in-app FAB), autocomplete, sticky label | AC#2 and AC#3 pass here |
| M4 | Recently Completed, Settings, label merge/rename/delete | |
| M5 | **Capture surfaces**: Android tile, iOS control + intents, PWA | The long pole; kernels + fixture parity |
| M6 | Perf gates, a11y pass, hardening | AC#1 signed off here |

M1 preceding M2 is deliberate: the parser is the piece three languages must agree on, and it is far cheaper to settle its edge cases against a JSON file than against a keyboard.

---

## 16. Decisions made in the PRD's silence

| # | Decision | Reversible? |
|---|---|---|
| D-1 | `kvvfs`/localStorage fallback when OPFS is unavailable | Yes, trivially |
| D-2 | Native kernel refuses to write on schema-version mismatch; opens the app instead | Yes |
| D-3 | `@` must be at a word boundary to start a token | Yes — fixture change |
| D-4 | Trailing `.,;:!?` stripped from a token | Yes — fixture change |
| D-5 | A line containing only a token sets sticky, creates no item | Yes — fixture change |
| D-6 | Sticky updates progressively within a multi-line submit and persists after it | Yes |
| D-7 | Single-line input; newlines enter only via paste; Return always commits | Costly to reverse (§6.1 is built around it) |
| D-8 | When autocomplete is open, Enter completes rather than submits | Yes |
| D-9 | Undo depth is 1; a new completion commits the previous | Yes |
| D-10 | Label **merge** confirms, like delete | Yes |
| D-11 | Share-sheet / Web Share Target ingestion **not built** in v1 (not in the PRD) | Yes |
| D-12 | Font scaling grows row height; titles never wrap to two lines | Yes |

## 17. Conflicts requiring a PRD decision

**[C-1] Caret at tap position (§7.4, "cursor placed at tap position").** Free on web, genuinely awkward on React Native. Proposal: implement it, timeboxed to three days per platform, with caret-at-end as the accepted fallback. Impact if it falls back: minor.

**[C-2] iOS Control Center → Quick Add overlay (§12, P0).** Not implementable as written; iOS controls cannot host a text field, and any surface that accepts typed text requires an unlocked device. Proposal: (a) accept the native-sheet-on-deep-link behaviour in §7.3 as the Control Center path, and (b) **promote the Shortcuts/Siri App Intent from P1 to P0**, since voice capture is the only iOS route that genuinely satisfies AC#1's "from a locked phone." This is a product decision, not an engineering one, and it changes what AC#1 means on iOS.
