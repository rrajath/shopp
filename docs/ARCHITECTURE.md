# Architecture

## Stack

Kotlin and Jetpack Compose, two Gradle modules (`:app` and `:designsystem`), one process, one Room database. No backend, no network access.

Because the app is native end to end, the in-app flow, the Quick Settings tile, and the home screen widget all run the same code against the same database:

- `MainActivity` and `CaptureActivity` (the tile's target) share the `QuickAddOverlay` composable, the use cases, and the `AppContainer` singleton.
- `parseCapture()` is a single pure Kotlin function, tested directly.
- Room's `Flow`-returning DAO queries invalidate on write and feed Compose via `StateFlow`/`collectAsState()`. No separate change-notification layer is needed.

## Package layout

```
com.rrajath.shopp/
├─ ShoppApplication.kt       Builds AppContainer (manual DI): db, clock, idGen,
│                             repositories, use cases. Shared by every Activity and the widget
├─ MainActivity.kt           Hosts ShoppApp, the in-app root composable
├─ capture/
│  ├─ QuickAddTileService.kt TileService: onClick() -> startActivityAndCollapse
│  ├─ CaptureActivity.kt     Translucent, showWhenLocked + turnScreenOn, hosts QuickAddOverlay
│  └─ CaptureViewModel.kt    Thin wrapper around QuickAddController for this Activity
├─ data/
│  ├─ db/                    Room: ShoppDatabase, ItemEntity, LabelEntity, ItemDao, LabelDao
│  └─ repository/            ItemRepository, LabelRepository, PreferencesRepository
├─ domain/                   parseCapture(), Uuidv7, Clock, LabelColorAllocator, LabelNameFolding
├─ usecases/                 One class per mutation: CaptureItems, CompleteItem, UndoComplete,
│                             EditTitle, RenameLabel, MergeLabels, DeleteLabel, ReaddCompleted
├─ widget/                   Glance widget: ShoppWidgetReceiver, ShoppWidget,
│                             ShoppWidgetContent, CompleteItemAction
└─ ui/
   ├─ components/            App-coupled composables (take domain types/use cases directly):
   │                          QuickAddOverlay, ItemRow, LabelChipRow, LabelManagementSheet, DrawerMenu
   ├─ screens/                ListScreen, RecentlyCompletedScreen, LabelsScreen, SettingsScreen
   ├─ ShoppApp.kt            Root shell: screen switch + drawer/Quick Add/undo overlay stack
   ├─ ShoppViewModel.kt      Screen, drawer, list, undo, and labels state for the in-app flow
   └─ QuickAddController.kt  Quick Add state and logic, shared by ShoppViewModel and CaptureViewModel

com.rrajath.shopp.designsystem/    (separate Gradle module, see below)
├─ theme/                    ShoppColors, ShoppType, ShoppDimens, ShoppTheme
└─ components/                Domain-free composables: ConfirmDialog, EmptyState,
                               HeaderRow, SectionHeader, UndoToast, LinkifiedText
```

`ItemEntity` and `LabelEntity` double as the domain model. They're plain data classes with no Android coupling beyond Room annotations, so a separate mapping layer would add nothing.

## Modules

`:designsystem` holds every design token and every composable that doesn't depend on app domain types, ViewModels, use cases, or navigation. `:app` depends on it, never the reverse, so the Gradle graph enforces that screens get colors and spacing from one place.

Components that take domain types (`ItemRow` takes `ItemEntity`, `LabelManagementSheet` takes `LabelEntity` and `RenameLabel`, `DrawerMenu` takes the `Screen` enum) stay in `:app`'s `ui/components/`. [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) explains why tokens and components look the way they do.

The Glance widget can't use Compose UI components (it renders to `RemoteViews`), so `ShoppWidgetContent.kt` has its own header, section, and row composables built from Glance primitives. It reuses only `ShoppColors` and `ShoppDimens`.

## Data layer

**Room.** Compile-time-checked queries and `Flow`-returning DAOs. Tables use `ON DELETE RESTRICT`. Room's `@Index` can't express a `WHERE` clause, so the partial indexes (including the partial unique index on folded label names) are created with raw SQL in `RoomDatabase.Callback.onCreate`. WAL mode is on, and `PRAGMA busy_timeout=3000` is set via `db.query(...).close()`, because `execSQL` throws for statements that return a row, which includes most `PRAGMA` setters.

**Row invariants.** Every row has a UUID primary key, an `updated_at` timestamp, and soft-delete tombstones. A debug-only trigger (gated on `BuildConfig.DEBUG`) enforces that `updated_at` changes on every mutation. `LabelRepository.resolveOrCreate` is the single owner of a label's timestamps: writing a label twice in the same millisecond within one transaction violates that invariant.

**IDs.** UUIDv7 with the monotonic-counter variant (RFC 9562 §6.2). Pasting several lines into Quick Add creates multiple items in one transaction, often in the same millisecond; the counter keeps them sorted in paste order.

**One database instance.** `MainActivity`, `CaptureActivity`, and the widget all read `AppContainer` from the same `ShoppApplication`, so they share one `ShoppDatabase`. There are no cross-process concerns.

## Domain and use cases

`parseCapture()` is pure and I/O-free: it turns raw text into titles paired with a `LabelRef` (`None`, `Id`, or `Token`). Resolving a `Token` to a label row, creating it if needed, happens in the calling use case via `LabelRepository.resolveOrCreate`. This keeps the parser trivially unit-testable and token rules in one place.

`@` only starts a label token after a word boundary (start of line or whitespace). `Milk@Groceries` stays a literal title, so email addresses aren't mistaken for labels.

Each use case in `usecases/` wraps its work in `ShoppDatabase.withTransaction { }`. For example, `CompleteItem` trims Recently Completed to 100 items in the same transaction as the completion; `MergeLabels` reassigns items and tombstones the source label atomically; `DeleteLabel` moves items to Inbox and removes the label atomically.

`CaptureItems` silently drops a line if an active item already has the same folded (case-insensitive) title under the same label, or if an earlier line in the same paste does. The same title under a different label, or one only in Recently Completed, is still added. This is enforced in the use case rather than a unique index, because completed and tombstoned rows with the same title must remain valid.

## UI and state

**Manual DI.** `AppContainer` is a plain class built once in `ShoppApplication.onCreate()`. At this size, Hilt/Dagger would add annotation processing without solving anything.

**`ShoppViewModel`** holds in-app screen, drawer, undo, and list state. Sections are built in Kotlin by `buildSections()` over a single combined Room `Flow`. Items are ordered `(label_id IS NOT NULL), created_at ASC, id ASC`, so Inbox leads and the rest follow in label-then-creation order.

**`QuickAddController`** is deliberately not a ViewModel. Quick Add must behave identically in `MainActivity` and `CaptureActivity`, and a ViewModel is scoped to one Activity. It's a plain class taking a `CoroutineScope` and the shared `AppContainer`; `ShoppViewModel` and `CaptureViewModel` each construct one and delegate to it.

**Caret at tap position.** Inline edit uses `Text`'s `onTextLayout` and `TextLayoutResult.getOffsetForPosition(Offset)` to place the caret where the user tapped.

**Lists.** `LazyColumn` with a `stickyHeader` per section.

## Quick Settings tile

`QuickAddTileService.onClick()` calls `startActivityAndCollapse` targeting `CaptureActivity`, using the `PendingIntent` overload on API 34+ and the deprecated `Intent` overload below that. `CaptureActivity` uses `Theme.Shopp.Capture` (translucent, transparent background, no animation) and calls `setShowWhenLocked(true)` / `setTurnScreenOn(true)` on API 27+ (window flags below that), so it appears over the lock screen. It hosts the same `QuickAddOverlay` with its own `QuickAddController` on the shared `AppContainer`.

`CaptureActivity` is `exported="false"`; only the app's own `TileService` can launch it.

It also sets `android:taskAffinity=""`. Without it, `CaptureActivity` inherits `MainActivity`'s affinity, and with `launchMode="singleTask"` plus `FLAG_ACTIVITY_NEW_TASK`, Android would bring an existing `MainActivity` task to the front first and stack the capture overlay on top of the full app. The empty affinity forces a new, isolated task.

## Home screen widget

Built on Glance (`androidx.glance:glance-appwidget`) so it can use the Compose color and spacing tokens directly. `ShoppWidgetReceiver` points at `ShoppWidget`, which reads `(context.applicationContext as ShoppApplication).container`: the same `AppContainer` as the Activities.

**Sections.** `ShoppWidget.provideGlance` calls the same `buildSections()` as `ShoppViewModel`, always grouped by label, and drops empty sections (the widget doesn't pin an empty Inbox). Everything renders in a Glance `LazyColumn`, so all items are reachable by scrolling.

**Tap to complete.** `CompleteItemAction` (an `ActionCallback`) calls the shared `CompleteItem` use case and triggers a widget update. The widget only queries active items, so the completed item simply disappears; there's no intermediate checked state.

**Freshness.** `provideContent { }` collects the item, label, and preference Flows directly, so an active Glance session recomposes on change. Separately, `ShoppApplication.onCreate()` starts an application-scoped coroutine that calls `ShoppWidget().updateAll(context)` whenever items or labels change, covering the case where the process is alive but the widget session isn't. `updatePeriodMillis` in `shopp_widget_info.xml` is set to Android's 30-minute floor as a fallback after process death.

**Fonts.** Glance's `TextStyle` has no `fontFamily`, so widget text uses the system font instead of Caprasimo/Figtree. All other tokens match the app.

**WorkManager is pinned directly.** Glance renders the widget in a WorkManager worker, and Glance 1.1.1 pulls in WorkManager 2.7.1, whose R8 rules don't keep `OverwritingInputMerger`'s no-arg constructor under R8 full mode. In minified release builds the worker failed to start and the widget stuck on its loading layout. `app/build.gradle.kts` declares `androidx.work:work-runtime-ktx` 2.11.x, which ships correct keep rules. Don't remove it unless a newer Glance brings in a fixed WorkManager.

## Testing

- **JVM unit tests** (`app/src/test`): `parseCapture()` golden fixtures and branch coverage, UUIDv7 monotonicity, label color allocation, and one test class per use case covering transaction atomicity, ordering, and the `updated_at` invariant.
- **Room/Robolectric tests**: schema, partial indexes, triggers, and trim-on-complete boundaries, using `ShoppDatabase.buildInMemory()`, which shares the production `RoomDatabase.Callback`.

Run them with `./gradlew :app:testDebugUnitTest`.
