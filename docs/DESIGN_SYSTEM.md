# Design System

Color, typography, spacing, motion, and component conventions for Shopp. Check here before adding or styling UI; if something isn't covered, add a token or entry here rather than inventing a one-off value.

**Code:** tokens live in the `:designsystem` module under `designsystem/src/main/java/com/rrajath/shopp/designsystem/`:

- `theme/`: `Color.kt` (`ShoppColors`), `Type.kt` (`ShoppType`), `Dimens.kt` (`ShoppDimens`), `Theme.kt` (`ShoppTheme`)
- `components/`: `ConfirmDialog`, `EmptyState`, `HeaderRow`, `SectionHeader`, `UndoToast`, `LinkifiedText`, `QuantityStepper`

Components that take app domain types (`ItemRow`, `LabelChipRow`, `LabelManagementSheet`, `QuickAddOverlay`, `DrawerMenu`) live in `app/src/main/java/com/rrajath/shopp/ui/components/`. The docs site (`docs-site/src/styles/custom.css`) mirrors the tokens as `--shopp-*` custom properties.

## Color

Light and dark palettes, selected by `ThemeMode` (System/Light/Dark in Settings). `System` follows `isSystemInDarkTheme()`.

`muted`, `line`, `checkboxBorder`, and `scrim` are the text color (or black) at partial alpha, not pre-blended solids, so they read correctly on any surface. `background`, `sheet`, and `menu` share one color per theme; floating surfaces (Quick Add card, popovers, drawer) separate from the page by shadow, not fill.

| Token | Light | Dark | Used for |
|---|---|---|---|
| `background` / `sheet` / `menu` | `#F9F4ED` | `#2E2B25` | Screen background, Quick Add card, popovers, drawer |
| `foreground` | `#201E1D` | `#F2E9DA` | Primary text |
| `muted` | `#201E1D` @ 55% | `#F2E9DA` @ 55% | Secondary text, hints, placeholders |
| `line` | `#201E1D` @ 16% | `#F2E9DA` @ 18% | Dividers, strikethrough on completed items, segmented-control border |
| `checkboxBorder` | `#201E1D` @ 28% | `#F2E9DA` @ 28% | Unchecked checkbox ring |
| `doneCheckboxFill` | `#CFC8BE` | `#645C50` | Recently Completed's filled checkbox |
| `chipUnselectedFill` | `#EEE7DB` | `#474238` | Unselected label chip fill |
| `chipUnselectedText` | `#474238` | `#EEE7DB` | Unselected label chip text |
| `scrim` | `#2E2B25` @ 32% | `#000000` @ 40% | Backdrop behind Quick Add, drawer, and label sheet. Always dims, never lightens |
| `toastBackground` | `#2E2B25` | `#F9F4ED` | Undo toast (inverted surface) |
| `toastForeground` | `#EEE7DB` | `#474238` | Undo toast text |
| `toastAction` | `#F6A06B` | `#B2622D` | "Undo" action |
| `accent` | `#C67139` | `#F6A06B` | FAB, active toggle/segment, cursor, merge radio |
| `onAccent` / `chipSelectedText` | `#F5EAD8` | `#211E19` | Text and icons on `accent` or a label color |
| `press` | `rgba(0,0,0,.04)` | `rgba(255,255,255,.05)` | Row press state |
| `shadow` | `#2E2B25` @ 22% | `rgba(0,0,0,.6)` | Elevation shadows |
| `inboxTint` | `#C0B6A5` | `#C0B6A5` | Inbox section header and unlabeled items |

The accent is fixed and not user-configurable.

### Label palette

15 pastel colors, identical in both themes. `labelPalette` in `Color.kt` backs every use of a label's color: auto-allocation, dots, section-header text, chip fills, and the color picker. A label's `colorIndex` means the same color everywhere.

| Index | Hex | Hue |
|---|---|---|
| 0 | `#E8A0A0` | rose |
| 1 | `#E8B88A` | orange |
| 2 | `#E8CC8A` | amber |
| 3 | `#DCE08A` | yellow-green |
| 4 | `#B8E08A` | lime |
| 5 | `#8FDB9E` | green |
| 6 | `#8AD9C2` | teal |
| 7 | `#8AD0E0` | cyan |
| 8 | `#8AB8E0` | sky blue |
| 9 | `#8A9FE0` | blue |
| 10 | `#A08AE0` | indigo |
| 11 | `#C28AE0` | purple |
| 12 | `#E08AD0` | magenta |
| 13 | `#E08AB8` | pink |
| 14 | `#C2A98A` | taupe |

`LabelColorAllocator` picks the lowest unused index, else `count mod 15`. Users can override it from the label sheet's color picker. Pastels have lower contrast when used as text (`SectionHeader`); that's an accepted trade-off.

## Typography

Two self-hosted families, licensed under OFL (`licenses/`):

- **Caprasimo** (headings): `res/font/caprasimo_regular.ttf`, weight 400 only.
- **Figtree** (everything else): `res/font/figtree_{light,regular,medium,semibold,bold}.ttf`, weights 300–700.

Named styles live in `ShoppType` (`Type.kt`), for example:

- `screenTitle`: Caprasimo 400, 21sp, `-0.02em`
- `sectionHeader`: Figtree 700, 12sp, `.08em`, uppercase
- `itemText`: Figtree 400, 18sp, line height 1.35

`settingsButtonLabel` (Figtree, theme segmented control) and `dialogActionLabel` (Caprasimo, sheet and dialog actions) are intentionally different families. Don't merge them.

Colors are not baked into `ShoppType`; callers apply `ShoppTheme.colors.*` explicitly.

## Spacing and sizing

All in `ShoppDimens` (`Dimens.kt`), grouped by component rather than a generic scale. `tapTarget = 44.dp` is the minimum touch target, applied via hit slop rather than taller rows.

| Component | Key values |
|---|---|
| Checkbox | `checkboxSize = 22.dp`, `checkboxBorderWidth = 1.5.dp` |
| FAB | `fabSize = 58.dp`, offsets `fabRightOffset = 22.dp` / `fabBottomOffset = 28.dp` |
| Header row | padding 20 horizontal / 14 top / 10 bottom dp; 40dp icon circle inside a 44dp tap target |
| List row | `rowPaddingHorizontal = 22.dp`, `rowGap = 14.dp` |
| Section header | padding 18 top / 22 horizontal / 7 bottom dp |
| Empty state | padding 34 top / 22 horizontal dp, left-aligned |
| Undo toast | pill (`toastCornerRadius = 100.dp`), padding 18/10 dp horizontal, 11dp vertical, `toastMaxWidth = 280.dp`, `toastBottomOffset = 100.dp` (16dp on screens without a FAB) |
| Quick Add card | `cardCornerRadius = 26.dp`, `cardHorizontalMargin = 12.dp`, 14dp padding, `cardMaxWidth = 400.dp` |
| Label chip | pill, `chipMinHeight = 28.dp`, padding 13/6 dp, `chipRowGap = 7.dp`, dot 7dp |
| Quick pick tabs | label-chip pills, `quickPickTabGap = 7.dp` apart, `quickPickTabsPaddingBottom = 10.dp` above the list |
| Quantity stepper | 28dp circles (`quantityStepperButtonSize`) in 44dp tap boxes, 16dp icons, count `minWidth = 20.dp`, `quantityStepperGap = 6.dp` from the Quick Add input |
| Merge picker | radio 16dp, 1.5dp border, 4dp inset ring; rows 11dp gap / 9dp vertical padding |
| Sheet buttons | pill, padding 18/9 dp, `sheetButtonGap = 14.dp` |
| Color picker | 24dp swatches, 5 per row, 32dp / 2dp selection ring |
| Drawer | 290dp wide, `drawerElevation = 16.dp` |

## Motion

- **Undo toast in:** slide up 20px + fade, ~200ms.
- **Backdrop fade:** ~160ms ease-out.
- **Drawer:** open progress (0 = closed, 1 = open) is a single `Animatable` owned by `ShoppApp.kt`; `DrawerMenu` is a pure function of that progress. Taps animate it with `tween(220)`. On the List screen, a right swipe `snapTo`s the same value so the panel tracks the finger, then settles open or closed (whichever side of halfway) over 180ms.
- **Label sheet:** slides up/down over 220ms, with a long-press haptic tick when it opens.
- **Row completion:** 150ms height + opacity exit. With reduced motion, rows are removed instantly and overlay fades drop their translation.
- **Quick Add card:** no dedicated transition; it appears and disappears with state.

## Components

- **Item row** (`ItemRow`): circular checkbox (fills with `accent` while completing) and a single-line title. A quantity above 1 shows as ` xN` after the title (e.g. "Milk x2"), in the title's own style; 1 shows nothing. In edit mode a `QuantityStepper` sits at the trailing end. The edit field uses an `accent` cursor. Dismissing the keyboard (back gesture or hide button) clears focus, commits the edit and exits edit mode, the same as Done or tapping away. Recently Completed adds a trailing label tag.
- **Quantity stepper** (`QuantityStepper`): `−  n  +`. Buttons are `chipUnselectedFill` circles with `chipUnselectedText` icons; the count is `quantityCount` (Figtree Medium 15sp) in `foreground`. At the limit (1 or 99) that button drops to 38% alpha and is disabled. Buttons don't take focus, so tapping them keeps a text field in edit mode.
- **Section header** (`SectionHeader`): bold uppercase micro-label, text tinted with the label's color (`inboxTint` for Inbox). No leading dot.
- **Empty state** (`EmptyState`): left-aligned `muted` primary line plus a smaller hint line.
- **Label chip** (`LabelChipRow`, Quick Add only): unselected chips are `chipUnselectedFill` with a leading dot in the label's color. Selected chips fill with the label's color and drop the dot. Inbox has no color, so its selected state uses `toastBackground`/`toastForeground`.
- **FAB:** 58dp `accent` circle, plus icon only.
- **Quick Add card** (`QuickAddOverlay`): compact floating card anchored above the keyboard (`WindowInsets.navigationBars.union(WindowInsets.ime)`), shadow-only elevation. The same component is used by the FAB, the Quick Settings tile, and the widget's add button. The input row ends in a `QuantityStepper` (default 1, resets after each submit). After a submit drops duplicates, the placeholder becomes a one-line `muted` hint ("Eggs x3 already added", or "Eggs x3 and 2 more already added") until the user types.
- **Suggestions popovers:** `@label` suggestions and title suggestions share one slot above the input (`menu` background, `press` on the top row, 15sp). Title suggestions appear when the current line is non-empty and not mid-`@token`, matching past Recently Completed titles by prefix, most recent first. Both popovers lead each row with an 8dp dot 10dp before the text: `@label` rows use the label's color, title rows use the color of the label the item was last bought under (`muted` for Inbox). Choosing a title suggestion replaces the line, moves the caret to the end, and files the item under that label.
- **Quick picks** (`QuickPicksPanel` in `QuickAddOverlay`): while the draft is empty, two tabs, **Frequent** (default) and **Recent**, sit over a popover styled like the suggestion popovers. The tabs are chip pills: the selected tab uses `toastBackground`/`toastForeground` with `chipSelected` text, like the selected Inbox chip, and the other uses `chipUnselectedFill`/`chipUnselectedText`. The list holds up to 10 rows, each an 8dp dot in the last-bought label's color (`muted` for Inbox) and a single-line, ellipsized 15sp title. The popover is capped at 5 rows tall and the rest scroll; switching tabs resets the scroll to the top. No row is highlighted. Tapping a row adds that item right away. Typing replaces the panel with the suggestion popovers in the same slot. The panel is hidden when there's no purchase history or every past item is already on the list.
- **Undo toast:** content-sized pill with the item's full title and an uppercase "Undo" action. Recently Completed reuses it to confirm re-adds ("Added to Groceries"), with Undo re-completing the item.
- **Drawer:** left-anchored, `menu` background with a `shadow`-tinted elevation so it separates from the same-colored page. Title in Caprasimo 26sp. Shows `Shopp {versionName} ({versionCode})` pinned at the bottom.
- **Label management sheet** (`LabelManagementSheet`): bottom sheet with Edit, Merge, and Delete.
  - **Edit:** rename field plus color picker. Tapping a swatch applies immediately; Save commits only the name.
  - **Merge:** radio per target label (outline in `line`; selected is `accent` with an inset ring), then confirm.
  - **Actions:** pill buttons in `dialogActionLabel`. Cancel is `line`-bordered; the confirm action is `accent` with `onAccent` text.
- **Settings theme control:** one pill-shaped segmented control (System/Light/Dark), no gaps; the selected segment is `accent`-filled with SemiBold text.
- **Settings toggles:** Material 3 `Switch`, recolored via `SwitchDefaults.colors()` (`accent`/`onAccent` when on, `checkboxBorder` when off).
- **System bars:** `ShoppTheme` updates status and navigation bar icon contrast in a `SideEffect` whenever the resolved theme changes, since `enableEdgeToEdge()` only follows the system theme at launch.
- **Home screen widget** (`widget/ShoppWidgetContent.kt`): rounded card with a header (title + add button), section headers styled like `SectionHeader`, and rows using `ItemRow`'s checkbox and padding values. All items scroll in a Glance `LazyColumn`. Card background alpha is set from Settings > Widget. Widget text uses the system font because Glance's `TextStyle` has no `fontFamily`; every other token matches.

## App icon

Adaptive icon (`res/drawable/ic_launcher_background.xml` + `ic_launcher_foreground.xml`): a circular checkbox with a checkmark. It uses fixed brand colors `#211E19` (background) and `#F6A06B` (ring and check) rather than referencing `ShoppColors`. `minSdk` 26 is the adaptive-icon minimum, so the `mipmap-*/ic_launcher*.webp` fallbacks are never shown.
