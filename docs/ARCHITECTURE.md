# Architecture

## Stack

Pure native Android: Kotlin + Jetpack Compose, two Gradle modules (`:app` and `:designsystem`), no multiplatform or cross-language surface at all.

The technical design document (`internal-docs/shopping-list-tdd.md`) specifies a React Native + web Turborepo monorepo, with native Kotlin/Swift used only for narrow "capture kernels" (the Quick Settings tile / widget surfaces) that write directly to a shared SQLite database — a deliberately expensive architecture chosen specifically to avoid booting an RN process from a locked phone. This repository, however, started as a bare native Android Studio scaffold with no RN/iOS/web tooling, and the product only targets Android. Given that, building the *whole* app natively rather than just the capture kernel isn't a smaller version of the TDD's plan — it removes the TDD's hardest engineering risk outright. There is only one process and one language, so:

- There's no "kernel vs. main app" split, and therefore no risk of the two drifting apart. `CaptureActivity` (the Quick Settings tile's target) and `MainActivity` share the same `QuickAddOverlay` composable, the same use-case classes, and the same `AppContainer` singleton.
- There's no cross-language parser to keep in sync. `parseCapture()` is one pure Kotlin function, tested directly, instead of a spec that has to be re-implemented identically in TypeScript and Kotlin/Swift and verified with golden fixtures across all three.
- There's no `DbChangeBus`/TanStack-Query-style invalidation layer to build. Room's `Flow`-returning DAO queries already auto-invalidate on write and propagate straight into Compose via `StateFlow`/`collectAsState()`.

Everything else — the schema, UUIDv7 ordering, the parser's exact token-matching rules, transaction boundaries per use case, undo semantics, and performance targets — comes directly from the TDD/PRD and is unaffected by the stack substitution.

## Package layout

```
com.rrajath.shopp/
├─ ShoppApplication.kt       Manual DI container (AppContainer): db, clock, idGen,
│                             repositories, use cases — built once, shared by both Activities
├─ MainActivity.kt           Hosts ShoppApp, the in-app root composable
├─ capture/
│  ├─ QuickAddTileService.kt TileService — onClick() -> startActivityAndCollapse
│  ├─ CaptureActivity.kt     Translucent, showWhenLocked+turnScreenOn, hosts QuickAddOverlay
│  └─ CaptureViewModel.kt    Thin wrapper around QuickAddController for this Activity
├─ data/
│  ├─ db/                    Room: ShoppDatabase, ItemEntity, LabelEntity, ItemDao, LabelDao
│  └─ repository/            ItemRepository, LabelRepository, PreferencesRepository
├─ domain/                   parseCapture(), Uuidv7, Clock, LabelColorAllocator, LabelNameFolding
├─ usecases/                 One class per mutation: CaptureItems, CompleteItem, UndoComplete,
│                             EditTitle, RenameLabel, MergeLabels, DeleteLabel, ReaddCompleted
├─ widget/                   Home-screen widget (Glance): ShoppWidgetReceiver, ShoppWidget,
│                             ShoppWidgetContent, CompleteItemAction
└─ ui/
   ├─ components/            App-coupled composables only (take domain types/use-cases directly):
   │                          QuickAddOverlay, ItemRow, LabelChipRow, LabelManagementSheet, DrawerMenu
   ├─ screens/                ListScreen, RecentlyCompletedScreen, LabelsScreen, SettingsScreen
   ├─ ShoppApp.kt            Root shell: screen switch + drawer/quickAdd/undo overlay stack
   ├─ ShoppViewModel.kt      Screen/drawer/list/undo/labels state for the in-app flow
   └─ QuickAddController.kt  Quick Add's state/logic, shared by ShoppViewModel and CaptureViewModel

com.rrajath.shopp.designsystem/    (separate Gradle module -- see "Modules" below)
├─ theme/                    ShoppColors, ShoppType, ShoppDimens, ShoppTheme
└─ components/                Generic, domain-free composables: ConfirmDialog, EmptyState,
                               HeaderRow, SectionHeader, UndoToast, LinkifiedText
```

`ItemEntity`/`LabelEntity` double as the domain model — they're plain data classes with no Android coupling beyond Room annotations, so a separate `domain/Item.kt`/`Label.kt` mapping layer would add a translation step with no real benefit at this size.

## Modules

`:designsystem` (August 2026) holds every design token and every Compose component with no dependency on app domain types, ViewModels, use-cases, or navigation state — `ShoppColors`/`ShoppDimens`/`ShoppType`/`ShoppTheme` plus `ConfirmDialog`, `EmptyState`, `HeaderRow`, `SectionHeader`, `UndoToast`, `LinkifiedText`. `:app` depends on it; the reverse is never true, so the Gradle dependency graph — not just convention — enforces that a screen can't reach past this module for a color or spacing value. Components that take domain types directly (`ItemRow` takes `ItemEntity`, `LabelManagementSheet` takes `LabelEntity` and the `RenameLabel` use case, `DrawerMenu` takes the `Screen` nav enum) stay in `:app`'s own `ui/components/`, since moving them would mean either moving domain types into the design-system module too (defeating the boundary) or laundering them through generic parameter types for no real benefit at this app's size. `docs/DESIGN_SYSTEM.md` is the rationale layer above this: it explains *why* a token or component looks the way it does, while the module is the enforced *source* of what it actually is.

The Glance widget (`widget/`) can't host Compose UI's own components (Glance renders to `RemoteViews`, not the Compose UI tree), so `ShoppWidgetContent.kt` reimplements its own header/section/item row composables locally with Glance's own `Text`/`Row`/`Column`/`Box` — it only reuses `:designsystem`'s `ShoppColors`/`ShoppDimens` for the underlying values, not the components themselves.

## Data layer

**Room, not raw SQLite.** Room gives compile-time-checked queries and `Flow`-returning DAO methods that invalidate automatically on write — this *is* the reactivity model the TDD describes (§5.1), for free. The schema is a direct translation of TDD §3.1: same tables, same `ON DELETE RESTRICT`, same partial indexes. Room's `@Index` annotation doesn't support a `WHERE` predicate, so the two partial indexes (and the partial unique index on folded label names) are created via raw SQL inside `RoomDatabase.Callback.onCreate`. WAL mode is enabled, with `PRAGMA busy_timeout=3000` set via `db.query(...).close()` (not `execSQL`, which throws for statements that return a result row — this includes most `PRAGMA` setters).

A debug-only trigger (gated on `BuildConfig.DEBUG`) enforces that `updated_at` is bumped on every row mutation. It's cheap insurance in development and caught a real bug during implementation: a label could be written twice at the same millisecond within a single transaction (once on creation/resolution, again by a separate "bump last-used" step), which — outside of debug builds — would occasionally throw a constraint violation in production too, whenever both writes landed in the same millisecond. The fix was structural (making `LabelRepository.resolveOrCreate` the single owner of touching a label's timestamps), not a trigger workaround.

**IDs.** UUIDv7 with the monotonic-counter variant (RFC 9562 §6.2). This matters for one specific case: pasting several lines into Quick Add at once creates multiple items inside a single transaction, often within the same millisecond — the counter variant guarantees they still sort in paste order.

**Single connection pool, no cross-process concerns.** `MainActivity` and `CaptureActivity` both read `AppContainer` off the same `ShoppApplication` instance, so they share one `ShoppDatabase` singleton. The TDD's multi-process concurrency section (§7.5) doesn't apply here — there is exactly one process and one Room instance, so the "does the tile write reach the main app" question is answered by construction rather than by a sync protocol.

## Domain and use cases

`parseCapture()` is a pure, I/O-free function: given raw captured text, it returns parsed titles paired with a `LabelRef` (a sealed type — `None`, `Id`, or `Token`). It never touches the database. Label *resolution* — turning a `Token("groceries")` into an actual label row, creating one if it doesn't exist — happens one layer up, in the use-case that calls `LabelRepository.resolveOrCreate`. This split keeps the parser trivially unit-testable (all of it is pure functions on strings) and keeps "what counts as a label token" logic in exactly one place.

One deliberate parser behavior worth calling out: `@` only starts a label token after a word boundary (start of line, or preceding whitespace). `Milk@Groceries` (no space) is therefore *not* split into a labeled item — it stays a literal title. This is intentional, to avoid treating things like email addresses as accidental label tags; the TDD's fixture set includes this exact case.

Each use case in `usecases/` corresponds to one row in TDD §4.3 and wraps its work in `ShoppDatabase.withTransaction { }`, matching the TDD's specified transaction boundaries exactly — e.g., `CompleteItem` runs the 100-item trim in the same transaction as the completion write; `MergeLabels` reassigns items to the target label and tombstones the source label atomically; `DeleteLabel` moves items to Inbox and removes the label atomically.

## UI and state

**Manual DI, no Hilt/Dagger.** `AppContainer` is a plain class instantiated once in `ShoppApplication.onCreate()`. At this project's size, a DI framework would add annotation processing and indirection without solving a problem manual construction doesn't already solve cleanly.

**`ShoppViewModel`** holds the in-app screen/drawer/undo/list state. Section grouping happens in Kotlin over a single Room `Flow` (`combine(observeActiveItems, observeLabels, ...)`), matching the TDD's "one pass, already in render order" approach — items are ordered `(label_id IS NOT NULL), created_at ASC, id ASC` so Inbox items lead and everything else follows in label-then-creation order.

**`QuickAddController`** is the one piece of state deliberately *not* modeled as a ViewModel. Quick Add needs to work identically from two different Activities (`MainActivity`'s FAB and `CaptureActivity`, launched from the Quick Settings tile), and a ViewModel is scoped to a single Activity/NavBackStackEntry — it can't be shared across two independent Activity instances. `QuickAddController` is a plain class parameterized by a `CoroutineScope` and the shared `AppContainer`; both `ShoppViewModel` and `CaptureViewModel` construct one and delegate to it. This is what makes "the Quick Settings tile uses the same component as the in-app Quick Add" (a PRD/TDD requirement) trivially true rather than something that needs cross-implementation verification — it's the literal same Kotlin object graph, just instantiated from a different Activity.

**Inline edit and caret-at-tap.** The TDD flags this as a real risk on React Native (§6.2, [C-1]), because RN doesn't expose a reliable tap-position → text-offset mapping. In Compose, `Text`'s `onTextLayout` callback yields a `TextLayoutResult` with `getOffsetForPosition(Offset)`, which does exactly that conversion directly — no fallback-to-caret-at-end path is needed.

**Lists.** Compose's `LazyColumn` with `stickyHeader` per section is already virtualized/recycling, so no third-party list library is needed (the TDD's FlashList justification was specifically about React Native's weaker default list recycling, which doesn't apply here).

## The Quick Settings tile

`QuickAddTileService.onClick()` calls `startActivityAndCollapse` — the `PendingIntent` overload on API 34+ (the `Intent` overload is deprecated from that version), falling back to the deprecated `Intent` overload below API 34 — targeting `CaptureActivity`. `CaptureActivity` uses `Theme.Shopp.Capture` (translucent, transparent window background, no animation) so it reads as a floating capture surface rather than a full app launch, and calls `setShowWhenLocked(true)` / `setTurnScreenOn(true)` on API 27+ (falling back to the equivalent `WindowManager.LayoutParams` flags below that) so it can appear directly over a locked screen without requiring the user to unlock first. It hosts the same `QuickAddOverlay` composable as the in-app flow, backed by its own `QuickAddController` instance pointed at the same `AppContainer` — so an item captured from the lock screen is written through the exact same use case, into the exact same database, that the main app reads from.

`CaptureActivity` is `exported="false"`: only the app's own `TileService` can launch it. This was verified on-device — `adb shell am start` targeting it directly from a different UID fails with a `SecurityException`, as expected.

**Real bug found and fixed**: `CaptureActivity` had no explicit `android:taskAffinity`, so it inherited the same default affinity as `MainActivity` (the app's package name). Combined with `launchMode="singleTask"` and `FLAG_ACTIVITY_NEW_TASK`, Android resolves the target task by *affinity*, not by which Activity is requested, so if `MainActivity`'s task already existed in the background, tapping the tile brought that whole task (and `MainActivity` with it) to the foreground first, then placed `CaptureActivity` on top of it in the same task. This read as "the app opens, then the dialog appears": a real, reproducible launch-path bug, not just a styling issue. Fixed with `android:taskAffinity=""` on `CaptureActivity`, which forces Android to always create a new, isolated task for it regardless of what other tasks already exist.

## The home-screen widget

Built on Glance (`androidx.glance:glance-appwidget`), not classic RemoteViews/XML, so it can read the app's existing Compose-based color/spacing tokens (`ShoppColors`, `ShoppDimens`) directly instead of duplicating them as a second set of Android resources. `ShoppWidgetReceiver` (a `GlanceAppWidgetReceiver`) just points at `ShoppWidget` (a `GlanceAppWidget`), which reads `(context.applicationContext as ShoppApplication).container` — the exact same `AppContainer` singleton `MainActivity` and `CaptureActivity` already share, so the widget is a third reader/writer of the same Room database rather than a separate data path.

**Sections and scrolling.** `ShoppWidget.provideGlance` calls the same top-level `buildSections()` function `ShoppViewModel` uses (extracted from a private method to a shared one for this purpose), always with `groupByLabel = true`, then drops empty sections — the widget has no "Inbox always pinned" behavior the List screen has, since wasting a section header on nothing is a worse trade at widget scale. `ShoppWidgetContent` renders every section header and item inside a Glance `LazyColumn` rather than capping the list to whatever fits the widget's current height, so all items are always reachable by scrolling instead of being replaced by a trailing "N more items" line past a fixed budget.

**Tap-to-complete.** Tapping an item runs `CompleteItemAction` (a Glance `ActionCallback`) which calls `container.completeItem(itemId)` — the same `CompleteItem` use case the in-app FAB uses — then explicitly triggers a widget update. Because the widget only ever queries `observeActiveItems()`, a completed item simply stops appearing on the next recomposition; there's no interim "checked" visual state to manage, unlike the in-app `ItemRow`'s optimistic-delay animation.

**Freshness.** Two mechanisms, not one: `provideContent { }` uses `collectAsState()` directly on the item/label/preference Flows, so an active Glance session recomposes reactively like any other screen; separately, `ShoppApplication.onCreate()` starts an application-scoped coroutine that collects `combine(observeActiveItems, observeLabels)` and calls `ShoppWidget().updateAll(context)` on every emission, covering the case where the app process is alive but the widget's own session currently isn't. `updatePeriodMillis` in `shopp_widget_info.xml` is set to Android's enforced floor (30 minutes) purely as a last-resort safety net after process death, not as the primary refresh path.

**Known Glance limitation.** `androidx.glance.text.TextStyle` has no `fontFamily` field — RemoteViews-backed widgets can't use the app's self-hosted Caprasimo/Figtree fonts, so widget text renders in the system default font. Every other token (color, size, weight, letter-spacing, the section-header's no-dot colored-text treatment) still matches the in-app `SectionHeader`/`ItemRow`.

## Testing

- **JVM unit tests** (`app/src/test`): `parseCapture()` against the full TDD §9.4 golden fixture set plus supplementary branch-coverage cases, UUIDv7 monotonicity, label color allocation, and one test class per use case covering transaction atomicity, ordering, and the `updated_at` invariant.
- **Room/Robolectric tests**: schema and partial-index/trigger behavior, trim-on-complete boundary conditions, using an in-memory `ShoppDatabase.buildInMemory()` that shares the same `RoomDatabase.Callback` as production.
- **Manual on-device verification**: every milestone in this build was verified on a running emulator (screenshots, direct interaction, and — for the Quick Settings tile specifically — `adb shell cmd statusbar` tile commands plus `dumpsys activity` ground-truth checks), not just compiled and unit-tested. This caught several real bugs unit tests didn't: a focus-detection false-positive that broke inline edit, a missing width modifier that made the drawer fill the screen, a missing background that let dark mode show a stray light strip, and a Quick Settings tile that rendered without a visible label because its runtime `Tile.state`/`Tile.label` were never set. See `PROGRESS.md` (gitignored, local build log) for the full list with root causes.
