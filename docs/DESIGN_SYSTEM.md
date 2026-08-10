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

Six colors, cycling (`LabelColorAllocator`: lowest unused index, else `count mod 6`). Refreshed alongside the August 2026 rebrand to read cleanly against the new neutral white/charcoal surfaces and to avoid colliding with either theme's accent (no red in the light palette, no yellow in the dark palette).

| Index | Light | Dark |
|---|---|---|
| 0 | `#1E5FA8` (blue) | `#3D7DD8` (blue) |
| 1 | `#2E7D32` (green) | `#4CAF50` (green) |
| 2 | `#6A1B9A` (purple) | `#9C4DCC` (purple) |
| 3 | `#00695C` (teal) | `#26A69A` (teal) |
| 4 | `#B45300` (orange) | `#E07B39` (orange) |
| 5 | `#AD1457` (pink) | `#D6487D` (pink) |

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
- Row completion: 150ms height+opacity exit (TDD §6.3); reduced-motion becomes an instant removal, and the overlay fade drops its translation (PRD §11 accessibility).

## Components inventory

- **Row** (`ItemRow`): circular checkbox (19dp, border in `checkboxBorder`, fills `accent` mid-animation), single-line text, optional trailing label tag (Recently Completed only).
- **Chip** (`LabelChipRow`): pill (`chipCornerRadius = 100.dp`), selected = filled with the label's color (or `foreground` for Inbox, text in `onForeground`) + `chipSelectedText`, unselected = outlined with `chipBorder` + `muted` text.
- **FAB**: pill-ish rounded rect (not circular), `accent` background, plus-icon + "Add" label, bottom-right offset 20dp.
- **Quick Add card** (`QuickAddOverlay`): compact floating card, **not** a full-width bottom sheet. Top-anchored (`cardTopMargin = 64dp` below the status bar), width capped at `cardMaxWidth = 400dp`, rounded corners (`cardCornerRadius = 20dp`), `sheet` background with shadow. Content-hugging height instead of stretching to the bottom of the screen. Shared identically by the in-app FAB and `CaptureActivity` (Quick Settings tile / lock-screen capture), the same component, so it always renders as a small overlay on top of whatever's behind it, never a full-screen surface.
- **Toast** (Undo): floating rounded rect above the FAB, `toastBackground`/`toastForeground`, uppercase "Undo" action in `toastAction`.
- **Drawer**: left-anchored, fixed 290dp width, `menu` background, title in Newsreader 26px. Slides in/out (see Motion).
- **Toggle** (Settings): custom track+knob (not Material Switch) — track 46×26dp radius 13dp, knob 17dp (on) / 11dp (off), matching the prototype's exact geometry rather than Material3's default switch shape.

## Known deliberate deviations from the PRD / original prototype

- Color palette was rebranded (August 2026, user request): charcoal + bright yellow (dark) / white + red (light), replacing the prototype's warm cream/terracotta. Typography, spacing, and component shapes are unchanged.
- Section headers are large serif display text (27px), not PRD §11's "smaller/heavier" micro-header.
- Quick Add is a compact top-anchored floating card, not the prototype's full-width bottom sheet (user request, to read clearly as an overlay rather than a full-screen surface — see Components inventory).
- Drawer entrance is a full off-screen slide, not the prototype's 16px micro-slide (user request; see Motion).
