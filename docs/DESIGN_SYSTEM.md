# Design System

Source of truth: `internal-docs/design/Shopp Prototype.dc.html`. Every value below is transcribed from that file's `renderVals()`. The PRD's greyscale/near-white/near-black spec (§11) was superseded by the prototype per an explicit product decision (prototype wins for scope + look — see `PROGRESS.md`).

Read this file before adding, modifying, or styling any UI element. Don't invent colors, spacing, or components it doesn't cover — extend this doc first, matching the prototype's language.

Implementation: `app/src/main/java/com/rrajath/milk/ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`, `Dimens.kt`).

## Color

Two full palettes (light / dark), switched by `ThemeMode` (System/Light/Dark, Settings screen). `System` follows `isSystemInDarkTheme()`.

| Token | Light | Dark | Used for |
|---|---|---|---|
| `background` | `#FCFAF7` | `#14110F` | Screen background |
| `foreground` | `#2A2724` | `#F0EAE4` | Primary text |
| `muted` | `#8A847C` | `#8E8579` | Secondary text, hints, placeholders |
| `line` | `#E4DED5` | `#332C27` | Strikethrough color on completed items |
| `checkboxBorder` | `#B3AEA5` | `#564E48` | Unchecked checkbox ring |
| `doneCheckboxFill` | `#CFC8BE` | `#4A423C` | Recently Completed's filled checkbox |
| `sheet` | `#FFFFFF` | `#241D19` | Quick Add sheet background |
| `menu` | `#FFFFFF` | `#241D19` | Drawer background, suggestions popover |
| `chipBorder` | `#E2DBD1` | `#4A3E37` | Unselected chip border |
| `scrim` | `rgba(30,26,23,.42)` | `rgba(0,0,0,.58)` | Backdrop behind sheet/drawer |
| `toastBackground` | `#2A2724` | `#EFE7E0` | Undo toast |
| `toastForeground` | `#FCFAF7` | `#241D19` | Undo toast text |
| `toastAction` | `#E9A98D` | `#8C4A32` | "Undo" label |
| `accent` | `#8C4A32` | `#E08A63` | FAB, active toggle, sticky chip (Inbox) |
| `onAccent` | `#FFFFFF` | `#241811` | Text/icons on the accent color |
| `press` | `rgba(0,0,0,.04)` | `rgba(255,255,255,.05)` | Row press state |
| `shadow` | `rgba(42,39,36,.18)` | `rgba(0,0,0,.6)` | All box-shadows |
| `inboxTint` | `#2A2724` | `#8E8579` | Inbox section header / unlabelled tint (prototype's `color(null)`) |

Accent is fixed — not user-configurable (matches PRD §8.2's "users cannot pick colors" applied consistently to the one non-label accent use).

### Label palette

Six colors, cycling (`LabelColorAllocator`: lowest unused index, else `count mod 6`). The PRD's "~10" was superseded by the prototype's actual 6-entry palette.

| Index | Light | Dark |
|---|---|---|
| 0 | `#B0442A` | `#E58C6B` |
| 1 | `#3F6B44` | `#86B889` |
| 2 | `#3C6382` | `#87ADCB` |
| 3 | `#7A5C2E` | `#C9A567` |
| 4 | `#6B4A75` | `#BE9BC7` |
| 5 | `#2F6B6B` | `#7FBDBD` |

## Typography

Two families:
- **Newsreader** (serif, headers/titles) — bundled as a variable font, `res/font/newsreader_variable.ttf` (OFL license: `licenses/newsreader-OFL.txt`). Two instances via `FontVariation`: weight 400 and weight 500, both at optical size 20sp. Bundled rather than fetched at runtime (offline-first, faster cold start — TDD §7.4 reasoning for bundling generally).
- **Roboto** (sans, everything else) — Android's system default (`FontFamily.Default`), matching the prototype's `Roboto, system-ui, sans-serif`. No bundling needed.

Named styles live in `ShoppType` (`Type.kt`) and are transcribed 1:1 from the prototype's inline `font` shorthands — e.g. `sectionHeader` = serif 400 27px/1, `itemText` = sans 300 18px/1.3, `settingsSectionLabel` = sans 400 12px/1 with `.14em` letter-spacing + uppercase. See that file for the full list; extend it there rather than hand-rolling `TextStyle`s inline.

Colors are **not** baked into `ShoppType` styles — callers apply `ShoppTheme.colors.*` explicitly, since most text swaps between `foreground` and `muted` depending on state (e.g. row text dims on completion).

## Spacing, radius, sizing

All in `Dimens.kt` (`ShoppDimens`), grouped by the screen/component they belong to (not a generic spacing scale — most of these are one-off measurements from the mockup, e.g. `fabHeight = 56.dp`, `drawerWidth = 290.dp`, `chipCornerRadius = 100.dp`). `tapTarget = 44.dp` is the one PRD-driven constant (§11) and is applied via `hitSlop`/minimum touch target, not by inflating row height — compact density is preserved.

## Motion

Matches the prototype's CSS keyframes:
- Sheet in: slide up 14px + fade, ~180ms, `cubic-bezier(.2,0,0,1)`
- Toast in: slide up 20px + fade, ~200ms, same easing
- Backdrop fade: ~160ms ease-out
- Drawer in: slide from -16px + fade, ~200ms, same easing
- Row completion: 150ms height+opacity exit (TDD §6.3); reduced-motion becomes an instant removal, and the overlay fade drops its translation (PRD §11 accessibility).

## Components inventory

- **Row** (`ItemRow`): circular checkbox (19dp, border in `checkboxBorder`, fills `accent` mid-animation), single-line text, optional trailing label tag (Recently Completed only).
- **Chip** (`LabelChipRow`): pill (`chipCornerRadius = 100.dp`), selected = filled with the label's color (or `foreground` for Inbox) + `onAccent`-ish text, unselected = outlined with `chipBorder` + `muted` text.
- **FAB**: pill-ish rounded rect (not circular), `accent` background, plus-icon + "Add" label, bottom-right offset 20dp.
- **Sheet** (Quick Add): bottom-anchored, `sheet` background, shadow up, suggestions popover floats above the input when open.
- **Toast** (Undo): floating rounded rect above the FAB, `toastBackground`/`toastForeground`, uppercase "Undo" action in `toastAction`.
- **Drawer**: left-anchored, fixed 290dp width, `menu` background, title in Newsreader 26px.
- **Toggle** (Settings): custom track+knob (not Material Switch) — track 46×26dp radius 13dp, knob 17dp (on) / 11dp (off), matching the prototype's exact geometry rather than Material3's default switch shape.

## Known deliberate deviations from the PRD

- Visual system is warm cream/terracotta + serif headers, not PRD §11's near-white/near-black + all-greyscale-except-dots. Prototype wins per the recorded product decision.
- Section headers are large serif display text (27px), not PRD §11's "smaller/heavier" micro-header.
