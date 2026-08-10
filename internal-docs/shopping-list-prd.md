# Shopping List — Product Requirements Document

**Version:** 1.0 (MVP scope frozen)
**Platforms:** Android, iOS, Web
**Status:** Ready for implementation

---

## 1. Product principle

**A shopping list is a conveyor belt, not a filing cabinet.**

Items have a lifespan measured in hours or days. They are created in a burst, consumed in a burst, and destroyed. The product earns exactly two capabilities: *put things on fast* and *take things off fast*. Every feature request must be justified against one of those two. Organizing, annotating, and rearranging are filing-cabinet behaviors and are out of scope by default.

This principle is the tiebreaker for any ambiguity not resolved by this document.

---

## 2. Goals

| # | Goal | Measure |
|---|---|---|
| G1 | Capture an item faster than any general-purpose notes app | ≤ 3s from cold surface tap to item committed |
| G2 | Add a run of items to one store without repeating the tag | N items to one label costs N lines of typing, one tag |
| G3 | Check off without thinking | Single tap, no confirmation, recoverable |
| G4 | Zero configuration required to be useful | App is fully functional on first launch with no setup |

## 3. Non-goals (v1)

Quantities as structured data · recipes · meal planning · price tracking · barcode scan · sharing/collaboration · reminders/notifications · location triggers · search · archive/history beyond the last 100 · manual reordering · nested lists · attachments · item images.

---

## 4. Information architecture

Three screens. No tabs; navigation via hamburger menu on the List screen.

```
List (home)
├── FAB → Quick Add overlay
└── ☰ Hamburger
    ├── Recently Completed
    └── Settings
```

External capture surfaces (Quick Settings tile etc.) open the Quick Add overlay directly, over the List screen.

---

## 5. Data model

Two entities. UUID primary keys and `updated_at` on every row from day one — these are free now and expensive to retrofit in v2.

### `items`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Client-generated |
| `title` | text | Free text, the entire user-visible content of an item |
| `label_id` | UUID? | Nullable. Null = Inbox |
| `state` | enum | `active` \| `completed` |
| `created_at` | timestamp | **Doubles as the sort key** — see §7.2 |
| `updated_at` | timestamp | Bumped on any field change |
| `completed_at` | timestamp? | Set when `state → completed` |
| `deleted_at` | timestamp? | Tombstone; see §5.1 |

### `labels`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | Client-generated |
| `name` | text | Without the `@`. Case-insensitive unique |
| `color_index` | int | Index into a fixed palette; see §8.2 |
| `created_at` | timestamp | |
| `updated_at` | timestamp | |
| `last_used_at` | timestamp | Bumped when an item is created with or moved to this label. Drives section ordering |
| `deleted_at` | timestamp? | Tombstone |

### 5.1 Deletion policy

All deletes are soft deletes (`deleted_at` set) even in v1, including the Recently Completed trim. Hard-deleting in v1 and adding sync in v2 will resurrect completed items on first sync. Reads filter `deleted_at IS NULL` everywhere.

---

## 6. Quick Add overlay

The most important surface in the product.

### 6.1 Anatomy

A compact overlay anchored above the keyboard, containing exactly two things:

1. A single-line-growing **text input**, autofocused, keyboard raised on open.
2. A single horizontal **row of label chips**, scrollable, ordered by `last_used_at` descending, with a leading "Inbox" (no label) chip.

Nothing else. No date picker, no quantity stepper, no priority, no "add note".

### 6.2 Behavior

| Trigger | Behavior |
|---|---|
| Open (FAB or external surface) | Overlay appears, input focused, keyboard up, in ≤ 300ms |
| Submit (Enter / keyboard "done") | Item(s) committed, input cleared, **overlay stays open, keyboard stays up, selected label persists** |
| Tap outside overlay | Overlay closes. Uncommitted input text is discarded |
| Back gesture / Esc | Same as tap outside |
| Tap a label chip | Chip becomes selected (sticky). Replaces any prior selection |
| Type `@` | Label autocomplete dropdown opens; see §6.4 |

**Sticky label is mandatory.** Selecting `@costco` once must apply to every subsequent submit in that overlay session until the user changes it. Without this, keeping the overlay open buys almost nothing — the user is still retyping the tag every line. On close, the sticky selection resets to Inbox.

### 6.3 Multi-line input

Newline is the **only** separator. Pasting or typing:

```
carrots
tomatoes
onions
```

...and submitting creates three separate items, in the order given, all with the currently selected label.

Explicitly **not** parsed: commas, semicolons, `and`, bullet characters, numbered prefixes. `carrots, 2lb organic bag` is one item titled exactly that. This ambiguity is the reason comma splitting was cut; the rule "one line = one item" needs no explanation and no preview UI.

Blank lines and leading/trailing whitespace are stripped. A submit that yields zero non-empty lines is a no-op.

### 6.4 Label syntax

- Typing `@` opens a dropdown of existing labels, filtered live as the user types more characters.
- Tap or Enter on a highlighted suggestion completes the token.
- On submit, the **first** `@token` in the input is extracted, removed from the title, and used as the item's label — overriding the sticky chip selection and *becoming* the new sticky selection.
- If the token matches no existing label (case-insensitive), a new label is created automatically with the next unused color.
- Any additional `@tokens` after the first are left as literal text in the title. **An item has exactly zero or one label.** This is a hard constraint, not a simplification — sections require it.
- With multi-line input, the token is extracted per line, so `@costco` on line 1 does not silently tag lines 2 and 3. Lines without a token use the sticky selection.

---

## 7. List screen

### 7.1 Row anatomy

One line. Checkbox on the left, title text filling the rest. No subtitle, no metadata, no chevron, no trailing actions.

URLs inside the title render as tappable hyperlinks; the rest of the title remains editable plain text. Tapping the link opens it; tapping elsewhere on the row enters edit mode.

### 7.2 Ordering

- **Within a section:** `created_at` ascending. Items always append to the end. There is no manual reordering, no sort menu, and no drag handle.
- Editing an item does not change its position.

### 7.3 Sections

- Grouped by label, rendered with sticky headers during scroll.
- **Inbox** (items with `label_id = NULL`) is always pinned first, even when empty of recent activity. It is the fast path for untagged capture.
- All other sections follow, ordered by `last_used_at` descending — the store you are actively adding to floats to the top.
- A section with zero active items is not rendered. The label still exists and still appears in autocomplete and Settings.

### 7.4 Item interactions

| Gesture | Result |
|---|---|
| Tap checkbox | Item completes: `state → completed`, `completed_at` set, row animates out of the list |
| Tap row | Inline edit — cursor placed at tap position, keyboard up. Commit on blur or Enter |
| Tap a URL in the row | Opens the link |
| Long press | **Nothing.** No multi-select, no drag |

**Undo is required.** Completing an item shows a 4-second toast with an UNDO action that restores the item to its original position. Check-to-delete without undo makes every mis-tap a trip to another screen; the toast removes the only anxiety in the primary interaction.

Editing a title to include an `@token` re-tags the item and moves it to that section, per §6.4. This is the sanctioned way to fix a mis-tag, and it is why drag-and-drop between sections was cut.

### 7.5 Empty state

A single line of text. No illustration, no onboarding carousel, no sample items.

---

## 8. Labels

### 8.1 Creation

Labels are only ever created implicitly, by typing an `@token` that doesn't exist. There is no "create label" button anywhere, including Settings.

### 8.2 Colors

- A fixed palette of ~10 colors ships with the app. New labels take the lowest-index color not currently in use; once exhausted, the palette cycles.
- Color appears as a small dot or the header text tint in the section header. It is a scanning aid, not decoration.
- **Users cannot pick colors.** In a monochrome UI the color is an 8px dot; a settings screen to adjust its hue is pure surface area.

### 8.3 Management (Settings)

| Operation | Behavior |
|---|---|
| Rename | Updates the label everywhere. No items move |
| Merge | Select target label; all items reassign to it; source label is tombstoned. **Required** — the real failure mode is `@costco` and `@costo` |
| Delete | Label tombstoned; its items become untagged and move to Inbox. Items are never deleted with the label. Confirm first |

Reorder is not offered; ordering is derived from `last_used_at`.

---

## 9. Recently Completed

- A flat, reverse-chronological list of completed items with relative timestamps ("2h ago", "yesterday").
- No grouping, no sections, no filtering, no search.
- **Retention: the most recent 100 completed items.** Older entries are tombstoned. Trimming happens opportunistically on write (when an item is completed), not on a schedule. There is no cron job — web has no cron and mobile background execution is unreliable.
- A count cap rather than a time window because grocery cycles are weekly and a 3-day window would routinely present an empty screen. A cap also never surprises the user with disappearing content.
- Tapping an entry **re-adds it to the active list** with its original label, appended to the end. This is the screen's real job: recurring items get a one-tap re-add. The entry remains in Recently Completed.

There is no "clear all" and no permanent history. This screen is a short-term buffer, not an archive.

---

## 10. Settings

Exactly two sections:

1. **Theme** — System / Light / Dark.
2. **Labels** — the list from §8.3.

Nothing else in v1. No export, no import, no font size, no haptics toggle, no about page beyond a version string.

---

## 11. Visual design

- Reference point: the checklist in Apple Notes. Near-pure white (`#FFFFFF`) and near-pure black (`#000000`) backgrounds.
- One text size for items, one smaller/heavier size for section headers. No dividers between rows; whitespace does the separating.
- Color appears only in label dots. Everything else is greyscale.
- Motion is functional only: row exit on complete, overlay in/out. No decorative animation, no confetti, no bounce.
- Tap targets ≥ 44pt despite the compact visual density.

---

## 12. External capture surfaces

This is the highest-leverage feature in the product and should receive the implementation budget freed by cutting drag-and-drop and multi-select. It is also the most platform-fragmented item on the list — expect three separate native implementations.

| Platform | Surface | Priority |
|---|---|---|
| Android | Quick Settings tile → Quick Add overlay | P0 |
| iOS | Control Center control → Quick Add overlay | P0 |
| iOS | Lock Screen widget, Action Button, Shortcuts/Siri intent | P1 |
| Web | Installable PWA; app shortcut to Quick Add | P1 |

The overlay opened from an external surface is the **same component** with the same behavior as the in-app FAB overlay, including sticky label and stay-open-on-submit. It must be usable without unlocking to the full app where the platform allows.

---

## 13. Explicitly cut from v1 — and why

Recorded so these don't quietly return during implementation.

| Cut | Rationale |
|---|---|
| Description / notes field per item | Quantity, brand, and links all fit in the free-text title. A second field forces expand/collapse state, taller rows, and a second edit mode — it breaks the one-line row that defines the aesthetic |
| Drag and drop between sections | Expensive across three platforms (drag + sticky headers + web), solves a weekly 4-second problem already solved by editing the `@token`, and collides with long-press |
| Long-press multi-select + bulk move | Duplicates the job of drag-and-drop. Bulk complete is already cheap: five taps beats long-press → four taps → find action |
| Cycling label button | O(n) taps. Fine at three labels, a slot machine at eight. The chip row is O(1) in the same pixel budget |
| Comma splitting | Genuinely ambiguous (`carrots, 2lb organic bag`). Newline-only needs no preview UI and no explanation |
| Daily cron / background purge | No cron on web, unreliable on mobile. Opportunistic trim on write is sufficient |
| Custom label colors | An override for a system that already works, controlling an 8px dot in a monochrome UI |
| 3-day time-based expiry | Shorter than the weekly grocery cycle; replaced by a 100-item cap |

---

# V2 — Deferred scope

Not to be built in v1. Listed so v1 architecture doesn't foreclose it.

## V2.1 Accounts

- Email/password plus social logins (Apple required for iOS, Google, optionally GitHub).
- **The app must remain fully functional signed-out.** Local-first is the v1 behavior and stays the default; an account is opt-in and only buys sync.
- On first sign-in, existing local data migrates into the account rather than being discarded.

## V2.2 Cloud sync

- Cloud-hosted DB, last-write-wins conflict resolution at the field level using `updated_at`. A shopping list does not need CRDTs; the cost of losing a conflicting edit to "carrots" is one retype.
- Offline writes queue locally and flush on reconnect. The UI never blocks on network.
- Tombstones (`deleted_at`) propagate as deletes. This is why v1 soft-deletes.
- Label merges must sync as reassignment + tombstone, not as a rename, or clients diverge.

### V1 obligations that make V2 cheap

These three are non-negotiable in v1:

1. **UUID primary keys**, client-generated. Never autoincrement.
2. **`updated_at` on every row**, bumped on every mutation.
3. **Tombstones instead of hard deletes**, including the Recently Completed trim.

## V2.3 Candidates, not commitments

Sharing a list with another account · a real archive with search · quantities as structured data.

Each must pass the conveyor-belt test independently at the time it is proposed. Sharing in particular introduces presence, permissions, and merge semantics that would double the surface area of the product.

---

## 14. Acceptance criteria

The build is done when all of these hold:

1. From a locked phone, adding "milk" to Inbox via the platform quick-capture surface takes ≤ 3 seconds and ≤ 3 interactions.
2. Adding six items to `@costco` requires typing the tag exactly once.
3. Pasting a six-line block creates six items in order.
4. Completing an item is one tap, is reversible for 4 seconds, and never asks for confirmation.
5. A newly installed app with zero configuration can capture and complete items immediately.
6. Every screen in the app is reachable in ≤ 2 taps from the List screen.
7. No screen in the app contains a feature not described in this document.
