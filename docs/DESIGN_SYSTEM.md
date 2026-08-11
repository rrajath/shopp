# Design System

Original source of truth: `internal-docs/design/Shopp Prototype.dc.html` (still authoritative for typography, spacing, and component shapes). **Color was intentionally rebranded in August 2026** (user request) away from the prototype's warm cream/terracotta palette to a neutral charcoal/white base with a bright accent. See the Color section below, which is now the source of truth for color instead of the prototype file.

Read this file before adding, modifying, or styling any UI element. Don't invent colors, spacing, or components it doesn't cover — extend this doc first, matching its language.

Implementation: `app/src/main/java/com/rrajath/milk/ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`, `Dimens.kt`).

## Color

Two full palettes (light / dark), switched by `ThemeMode` (System/Light/Dark, Settings screen). `System` follows `isSystemInDarkTheme()`. Light mode: white background + red accent. Dark mode: charcoal background + bright yellow accent. Every other token is re-derived from that base (not left over from the old warm-brown palette).

| Token | Light | Dark | Used for |
|---|---|---|---|
| `background` | `#FFFFFF` | `#212121` | Screen background |
| `foreground` | `#1E1E1E` | `#F2F2F2` | Primary text |
| `muted` | `#767676` | `#A0A0A0` | Secondary text, hints, placeholders |
| `line` | `#E4E4E4` | `#3A3A3A` | Strikethrough color on completed items |
| `checkboxBorder` | `#C4C4C4` | `#5C5C5C` | Unchecked checkbox ring |
| `doneCheckboxFill` | `#D8D8D8` | `#454545` | Recently Completed's filled checkbox |
| `sheet` | `#FFFFFF` | `#2A2A2A` | Quick Add card background |
| `menu` | `#FFFFFF` | `#2A2A2A` | Drawer background, suggestions popover |
| `chipBorder` | `#DDDDDD` | `#4A4A4A` | Unselected chip border |
| `scrim` | `rgba(30,30,30,.42)` | `rgba(0,0,0,.6)` | Backdrop behind Quick Add card / drawer |
| `toastBackground` | `#1E1E1E` | `#F2F2F2` | Undo toast |
| `toastForeground` | `#FFFFFF` | `#212121` | Undo toast text |
| `toastAction` | `#FFD400` | `#E53935` | "Undo" label (each theme borrows the *other* theme's accent, since the toast surface itself inverts: dark toast in light mode, light toast in dark mode) |
| `accent` | `#E53935` | `#FFD400` | FAB, active toggle, sticky chip (Inbox), cursor |
| `onAccent` | `#FFFFFF` | `#212121` | Text/icons on the accent color |
| `chipSelectedText` | `#FFFFFF` | `#FFFFFF` | Text on a selected label chip (colored fill) |
| `onForeground` | `#FFFFFF` | `#212121` | Text on a `foreground`-colored surface, specifically the Inbox chip's selected fill, which uses `foreground` as its background. `chipSelectedText` isn't safe there since both `chipSelectedText` and `foreground` are light in dark mode (this fixed a real light-text-on-light-fill contrast bug) |
| `press` | `rgba(0,0,0,.04)` | `rgba(255,255,255,.05)` | Row press state |
| `shadow` | `rgba(30,30,30,.18)` | `rgba(0,0,0,.6)` | All box-shadows |
| `inboxTint` | `#1E1E1E` | `#A0A0A0` | Inbox section header / unlabelled tint (prototype's `color(null)`) |

Accent is fixed — not user-configurable (matches PRD §8.2's "users cannot pick colors" applied consistently to the one non-label accent use).

### Label palette

Fifteen colors, cycling (`LabelColorAllocator`: lowest unused index, else `count mod 15`). Expanded from the original 6 in August 2026 (user request, alongside the Labels "Edit" color picker — see Components inventory) to muted/pleasant tones rather than bright/saturated ones. Each index is a single hue family: light mode uses a deepened shade (for contrast on white/charcoal-adjacent text use, e.g. section headers and label tags), dark mode a lightened shade of the same hue (for contrast on the dark surface) — same pattern as the original 6.

| Index | Light | Dark | Hue |
|---|---|---|---|
| 0 | `#B15D66` | `#D98D96` | dusty rose |
| 1 | `#B35A38` | `#E0916A` | terracotta |
| 2 | `#A67C1E` | `#D9AC4E` | mustard |
| 3 | `#6E7A3D` | `#A8B571` | olive |
| 4 | `#4C7A4A` | `#8FBF86` | sage |
| 5 | `#3D8A75` | `#7FD1B9` | seafoam |
| 6 | `#2E7A78` | `#6FBDBA` | teal |
| 7 | `#3E689A` | `#7FAAD6` | steel blue |
| 8 | `#5259AD` | `#9BA3E8` | periwinkle |
| 9 | `#7455A0` | `#B99CDB` | lavender |
| 10 | `#8C4F80` | `#CB9AC0` | mauve |
| 11 | `#B36A46` | `#E3AE8B` | dusty peach |
| 12 | `#556072` | `#9FADC2` | slate |
| 13 | `#71624F` | `#BBA98E` | warm gray |
| 14 | `#99503B` | `#D68F73` | clay |

Users can now also pick a label's color explicitly via the Labels screen's "Edit" sheet (a 5-per-row swatch grid of the table above), rather than only ever getting the auto-allocated one.

## Typography

Two families:
- **Newsreader** (serif, headers/titles) — bundled as a variable font, `res/font/newsreader_variable.ttf` (OFL license: `licenses/newsreader-OFL.txt`). Two instances via `FontVariation`: weight 400 and weight 500, both at optical size 20sp. Bundled rather than fetched at runtime (offline-first, faster cold start — TDD §7.4 reasoning for bundling generally).
- **Roboto** (sans, everything else) — Android's system default (`FontFamily.Default`), matching the prototype's `Roboto, system-ui, sans-serif`. No bundling needed.

Named styles live in `ShoppType` (`Type.kt`) and are transcribed 1:1 from the prototype's inline `font` shorthands — e.g. `sectionHeader` = serif 400 27px/1, `itemText` = sans 300 18px/1.3, `settingsSectionLabel` = sans 400 12px/1 with `.14em` letter-spacing + uppercase. See that file for the full list; extend it there rather than hand-rolling `TextStyle`s inline.

Colors are **not** baked into `ShoppType` styles — callers apply `ShoppTheme.colors.*` explicitly, since most text swaps between `foreground` and `muted` depending on state (e.g. row text dims on completion).

## Spacing, radius, sizing

All in `Dimens.kt` (`ShoppDimens`), grouped by the screen/component they belong to (not a generic spacing scale — most of these are one-off measurements from the mockup, e.g. `fabHeight = 56.dp`, `drawerWidth = 290.dp`, `chipCornerRadius = 100.dp`). `tapTarget = 44.dp` is the one PRD-driven constant (§11) and is applied via `hitSlop`/minimum touch target, not by inflating row height — compact density is preserved.

## Motion

Matches the prototype's CSS keyframes, except where noted:
- Quick Add card in/out: no dedicated transition yet (open/close is driven by state, not animated); the card itself appears/disappears with its parent recomposition.
- Toast in: slide up 20px + fade, ~200ms, same easing
- Backdrop fade: ~160ms ease-out
- **Drawer in/out: slide the full panel width (from fully off-screen to docked) + independent scrim fade, 220ms** (`AnimatedVisibility` + `slideInHorizontally`/`slideOutHorizontally` on the panel, `fadeIn`/`fadeOut` on the scrim, in `DrawerMenu.kt`). Deliberately larger than the prototype's original 16px micro-slide, since the small offset read as "just appearing" rather than a perceptible slide, which is what was explicitly requested.
- **Swipe-right-to-open** (List screen only, August 2026 user request): a rightward drag starting anywhere on the List screen opens the drawer once it exceeds an 80dp threshold, in addition to the hamburger tap. Implemented with `detectHorizontalDragGestures` in `ListScreen.kt` rather than a `ModalNavigationDrawer`'s built-in edge-swipe, to keep the hand-rolled `DrawerMenu` component.
- Row completion: 150ms height+opacity exit (TDD §6.3); reduced-motion becomes an instant removal, and the overlay fade drops its translation (PRD §11 accessibility).

## Components inventory

- **Row** (`ItemRow`): circular checkbox (19dp, border in `checkboxBorder`, fills `accent` mid-animation), single-line text, optional trailing label tag (Recently Completed only).
- **Chip** (`LabelChipRow`): pill (`chipCornerRadius = 100.dp`), selected = filled with the label's color (or `foreground` for Inbox, text in `onForeground`) + `chipSelectedText`, unselected = outlined with `chipBorder` + `muted` text.
- **FAB**: pill-ish rounded rect (not circular), `accent` background, plus-icon + "Add" label, bottom-right offset 20dp.
- **Quick Add card** (`QuickAddOverlay`): compact floating card, **not** a full-width bottom sheet. **Bottom-anchored, directly above the keyboard** (August 2026 user request — was top-anchored below the status bar, which put it far from the IME; now `.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))` with `cardBottomMargin = 20dp`), width capped at `cardMaxWidth = 400dp`, rounded corners (`cardCornerRadius = 20dp`), `sheet` background with shadow. Content-hugging height instead of stretching to the bottom of the screen. Shared identically by the in-app FAB and `CaptureActivity` (Quick Settings tile / lock-screen capture), the same component, so it always renders as a small overlay on top of whatever's behind it, never a full-screen surface.
- **Toast** (Undo): floating rounded rect above the FAB, `toastBackground`/`toastForeground`, uppercase "Undo" action in `toastAction`. Also used in Recently Completed (August 2026) to confirm re-adding an item (e.g. "Added to Groceries") with an Undo action that re-completes it; positioned bottom-center without the FAB's extra clearance since that screen has no FAB.
- **Drawer**: left-anchored, fixed 290dp width, `menu` background, title in Newsreader 26px. Slides in/out (see Motion). Opens via hamburger tap or a right-swipe anywhere on the List screen (see Motion).
- **Label Edit sheet** (`LabelManagementSheet`, August 2026): the Labels screen's long-press menu item was renamed "Rename" → "Edit" and now combines the name text field with a 15-swatch color grid (5 per row, `labelColorSwatchSize = 32dp` circles, selected swatch ringed with `foreground` at `labelColorSwatchSelectedBorderWidth = 2dp`). Color taps apply immediately (no separate save step); the name still requires "Save" to commit (collision-checked, same as before).
- **Toggle** (Settings): custom track+knob (not Material Switch) — track 46×26dp radius 13dp, knob 17dp (on) / 11dp (off), matching the prototype's exact geometry rather than Material3's default switch shape.

## Known deliberate deviations from the PRD / original prototype

- Color palette was rebranded (August 2026, user request): charcoal + bright yellow (dark) / white + red (light), replacing the prototype's warm cream/terracotta. Typography, spacing, and component shapes are unchanged.
- Section headers are large serif display text (27px), not PRD §11's "smaller/heavier" micro-header.
- Quick Add is a compact floating card, not the prototype's full-width bottom sheet (user request, to read clearly as an overlay rather than a full-screen surface — see Components inventory). Originally top-anchored; changed to bottom-anchored above the keyboard in August 2026 (user request).
- Drawer entrance is a full off-screen slide, not the prototype's 16px micro-slide (user request; see Motion). Also opens via right-swipe in addition to the hamburger tap (August 2026 user request).
- Label colors were originally auto-allocated only, with no user picker and a 6-color palette; expanded to 15 muted colors with an explicit picker in the Labels "Edit" sheet (August 2026 user request — see Label palette, Components inventory).
