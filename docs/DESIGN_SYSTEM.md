# Design System

Source of truth for color, typography, spacing, and component shapes: `internal-docs/website/design_handoff_shopp_site/design/ShoppApp.dc.html` (the phone mockup, all six modes -- byte-identical to the earlier `internal-docs/website/ShoppApp.dc.html`) plus its accompanying `_ds/organic-*/styles.css` and `readme.md` (the Organic design system's full token sheet) for anything it depicts, falling back to the older `internal-docs/design/Shopp Prototype.dc.html` for anything it doesn't (Labels list rows, Recently Completed, Drawer). **August 2026, twice: color was rebranded away from this palette and back again, then this literal pass corrected both themes and typography against a since-arrived copy of the design system's own stylesheet.** An earlier session moved to a neutral charcoal/white base with a bright accent; that rebrand was reverted at explicit user request, back to the prototype's warm cream/terracotta/olive family, but light mode stayed a *reconstruction* (best-guess hex values) because the stylesheet holding the real light-mode tokens wasn't in the repo yet. A later design handoff (`internal-docs/website/design_handoff_shopp_site/`) included that stylesheet, plus the fonts (Caprasimo/Figtree) the app had never actually matched. This doc, and `Color.kt`/`Type.kt`/`Dimens.kt`, reflect that literal pass.

Read this file before adding, modifying, or styling any UI element. Don't invent colors, spacing, or components it doesn't cover -- extend this doc first, matching its language.

Implementation: `app/src/main/java/com/rrajath/shopp/ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`, `Dimens.kt`). The docs-site (`docs-site/src/styles/custom.css`) mirrors these tokens under `--shopp-*` custom properties, which also drive the Starlight docs section via `--sl-*` overrides.

## Color

Two full palettes (light / dark), switched by `ThemeMode` (System/Light/Dark, Settings screen). `System` follows `isSystemInDarkTheme()`. **Both themes are now a literal transcription** of `organic-*/styles.css`'s `:root` block (light) and `ShoppApp.dc.html`'s `[data-theme="dark"]` override block (dark) -- a structured neutral-100..900 / accent-100..900 / accent-2-100..900 tonal scale on each. Previously light mode was a documented reconstruction (best-guess hex values anchored on the older prototype) because the stylesheet holding the real light tokens wasn't in the repo; it has since arrived with `design_handoff_shopp_site/`.

Several tokens are alpha-based rather than solid colors, matching the source CSS's `color-mix(in srgb, var(--color-text) N%, transparent)` pattern literally (an N%-opacity version of the text color, not a pre-flattened blend against one specific background) -- `muted`, `line`, `checkboxBorder`, and (light-only shadow aside) `scrim` are all `Color` values with a fractional alpha channel, not `0xFF...` solids.

A structural finding from the literal pass: the phone frame in `ShoppApp.dc.html` sets its own background to `var(--color-neutral-100)`, not `var(--color-bg)` -- there's no "page behind the phone" in the real app the way there is on the marketing site, so `background` below is neutral-100. That also makes `background` and `sheet` the same color in both themes now: elevation for the Quick Add card / suggestions popovers reads via shadow only, not a lighter/darker fill, which is literally how the prototype draws it.

| Token | Light | Dark | Used for |
|---|---|---|---|
| `background` / `sheet` / `menu` | `#F9F4ED` | `#2E2B25` | Screen background; Quick Add card & suggestions popover background (`--color-neutral-100`, not `--color-bg` -- see above) |
| `foreground` | `#201E1D` | `#F2E9DA` | Primary text (`--color-text`) |
| `muted` | `#201E1D` @ 55% | `#F2E9DA` @ 55% | Secondary text, hints, placeholders -- Organic's own general-purpose `.text-muted`/`figcaption` value, not one of the several other one-off opacities (45/50/62/70%) the mockup uses contextually |
| `line` | `#201E1D` @ 16% | `#F2E9DA` @ 18% | Strikethrough color on completed items, dividers, segmented-control border (`--color-divider`) |
| `checkboxBorder` | `#201E1D` @ 28% | `#F2E9DA` @ 28% | Unchecked checkbox ring (literal: `color-mix(text, 28%, transparent)`) |
| `doneCheckboxFill` | `#CFC8BE` | `#645C50` | Recently Completed's filled checkbox -- not depicted by the new prototype, kept from the old one |
| `chipUnselectedFill` | `#EEE7DB` | `#474238` | Unselected label chip fill (`--color-neutral-200`; chips are filled, not outlined -- see Components inventory) |
| `chipUnselectedText` | `#474238` | `#EEE7DB` | Unselected chip text (`--color-neutral-800` -- distinct from `foreground`) |
| `scrim` | `#2E2B25` @ 32% | `#F9F4ED` @ 32% | Backdrop behind Quick Add card / drawer (`--color-neutral-900` @ 32%, both themes now -- previously asymmetric percentages guessed per theme). The Merge dialog's own scrim is literally 46%; `LabelManagementSheet` reuses this 32% value for both of its scrims rather than adding a second token for one sub-mode. |
| `toastBackground` | `#2E2B25` | `#F9F4ED` | Undo toast (`--color-neutral-900`; inverts: opposite-theme-colored surface) |
| `toastForeground` | `#EEE7DB` | `#474238` | Undo toast text (`--color-neutral-200`) |
| `toastAction` | `#F6A06B` | `#B2622D` | "Undo" label (`--color-accent-400`, each theme) |
| `accent` | `#C67139` | `#F6A06B` | FAB, active toggle/segment, selected chip fill, cursor, merge radio (`--color-accent` itself, the base token -- not an -N00 ramp rung) |
| `onAccent` / `chipSelectedText` | `#F5EAD8` | `#211E19` | Text/icons on the accent color (`--color-bg` -- distinct from `background`, which is neutral-100) |
| `press` | `rgba(0,0,0,.04)` | `rgba(255,255,255,.05)` | Row press state -- not specified by the prototype (no touch states), kept from before |
| `shadow` | `#2E2B25` @ 22% | `rgba(0,0,0,.6)` | `--shadow-lg` (`--color-neutral-900` @ 22% light; literal `rgba` dark) |
| `inboxTint` | `#C0B6A5` | `#C0B6A5` | Inbox section header / unlabelled tint -- a literal hardcoded hex in the prototype's script (`color(null)`), not a CSS var, identical in both themes |

Accent is fixed -- not user-configurable.

### Label palette

**Flat 15 pastel colors, the same values in both themes** (August 2026, user request -- deviates from `ShoppApp.dc.html`'s own 10-color `PALETTE` constant, which the app matched literally until now). One `List<Color>` (`labelPalette` in `Color.kt`) backs every use of a label's color across the app: auto-allocation (`LabelColorAllocator`), list-row/Labels-screen dots, section-header text, Quick Add chip fills, and the manual color-picker swatches in `LabelManagementSheet` -- a label's `colorIndex` means the same color everywhere.

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

`LabelColorAllocator`: lowest unused index, else `count mod 15`. **Has a user-facing color picker** -- `LabelManagementSheet`'s Edit view (see Components inventory) lets a user override a label's auto-assigned color to any palette entry, applied immediately on tap. Pastel colors are inherently lighter than the old earthy palette, so contrast is tighter wherever a color is used as *text* (`SectionHeader`) rather than a small fill/dot -- an accepted trade-off of this request, not re-litigated here.

## Typography

Two families -- **changed August 2026** (see History): the app used to bundle Newsreader (serif) + Roboto (system sans) on the belief that both prototypes agreed on those. They didn't: `ShoppApp.dc.html` has always declared `font-family: var(--font-heading)` / `var(--font-body)`, which the design system (`organic-*/styles.css`) defines as Caprasimo and Figtree. That only became visible once the design handoff bundle arrived with the stylesheet to check against.

- **Caprasimo** (display, headings) -- self-hosted, `res/font/caprasimo_regular.ttf` (OFL license: `licenses/caprasimo-OFL.txt`). One weight only (400); the prototype never asks for heading emphasis beyond that.
- **Figtree** (body, everything else) -- self-hosted, `res/font/figtree_{light,regular,medium,semibold,bold}.ttf` (OFL license: `licenses/figtree-OFL.txt`). Five static weights (300/400/500/600/700), covering every weight the app's styles use.

Both are `FontFamily`s (`CaprasimoFamily`, `FigtreeFamily`) built from static per-weight files -- no `FontVariation` needed, unlike the old variable-font Newsreader setup.

Named styles live in `ShoppType` (`Type.kt`). Where `ShoppApp.dc.html` depicts an element literally, size/weight/spacing are transcribed 1:1 from its inline styles -- e.g. `screenTitle` = Caprasimo 400 21px, `-0.02em`; `sectionHeader` = Figtree 700 12px, `.08em` uppercase (a small bold micro-label, not the large serif heading it used to be -- see History); `itemText` = Figtree 400 18px/1.35 (bumped from the prototype's 16px, August 2026 user request, keeping the 1.35 line-height ratio). Where it doesn't (Drawer, Labels screen rows, Recently Completed, Settings toggle rows), only the font family changed and the old prototype's sizes/weights are kept. See `Type.kt` for the full list and its inline comments for which category each style is in; extend it there rather than hand-rolling `TextStyle`s inline.

Two styles exist specifically because the prototype uses *different* font families for what look like similar "buttons": `settingsButtonLabel` (Figtree, the Theme segmented control -- literal, no `font-family` override there) vs. `dialogActionLabel` (Caprasimo, the Merge dialog's Cancel/Merge -- literal `font-family: var(--font-heading)`, matching Organic's shared `.btn` class). Don't merge them.

Colors are **not** baked into `ShoppType` styles -- callers apply `ShoppTheme.colors.*` explicitly.

## Spacing, radius, sizing

All in `Dimens.kt` (`ShoppDimens`), grouped by the screen/component they belong to (not a generic spacing scale). `tapTarget = 44.dp` is the one PRD-driven constant (§11), applied via `hitSlop`/minimum touch target rather than inflating row height.

Measurements `ShoppApp.dc.html` depicts explicitly win over the old prototype wherever they disagree:

- **Checkbox**: `checkboxSize = 22.dp`, `checkboxBorderWidth = 1.5.dp` (was 19dp/1dp, the old prototype's numbers). Applied to both `ItemRow` and Recently Completed's done-checkbox for consistency.
- **FAB**: `fabSize = 58.dp`, circular, icon-only, offset `fabRightOffset = 22.dp` / `fabBottomOffset = 28.dp` (asymmetric -- was a single 22dp offset on every side).
- **Header row**: `headerHorizontalPadding = 20.dp`, `headerTopPadding = 14.dp`, `headerBottomPadding = 10.dp` (literal `padding: 14px 20px 10px`); the hamburger/back icon's own visual circle is `headerIconVisualSize = 40.dp` inside the unchanged 44dp tap target (`headerLeadTrailSize`), rather than the two being the same size.
- **List row**: `rowPaddingHorizontal = 22.dp`, `rowGap = 14.dp` (was 24/16dp).
- **Section header**: small micro-label padding (`sectionHeaderPaddingTop/Horizontal/Bottom = 18/22/7.dp`, was 26/24/10dp), plus a new `sectionHeaderDotSize = 8.dp` identity dot and `sectionHeaderGap = 8.dp` -- see Components inventory.
- **Empty state**: `emptyStatePaddingTop/Horizontal = 34/22.dp` (was hardcoded 120/40dp in the composable, not even in `Dimens.kt`), left-aligned rather than centered.
- **Undo toast**: a content-sized pill, not a full-width bar -- `toastPaddingStart/End = 18/10.dp`, `toastPaddingVertical = 11.dp`, `toastGap = 16.dp`, `toastCornerRadius = 100.dp` (was a 14dp rounded rect), `toastBottomOffset = 100.dp` (Recently Completed's re-add toast, which has no FAB to clear, keeps its own smaller `toastBottomOffsetNoFab = 16.dp`).
- **Settings section label**: `settingsSectionLabelPaddingTop = 12.dp` for the first section (was 22dp; the second section's `PaddingTopSecond = 30.dp` was already correct).
- **Quick Add card**: `cardCornerRadius = 26.dp`, `cardHorizontalMargin = 12.dp`, `sheetPaddingHorizontal/Top/Bottom = 14.dp` uniform, `chipRowGap = 7.dp`, `chipPaddingHorizontal/Vertical = 13/6.dp`, `chipMinHeight = 28.dp`.
- **Settings theme control**: single pill-shaped segmented control (`themeSegmentContainerCornerRadius = 100.dp`, one outer border, no gaps) instead of 3 separately bordered buttons.
- **Merge picker**: radio `mergeRadioSize = 16.dp`, `mergeRadioBorderWidth = 1.5.dp`, `mergeRadioInsetRingWidth = 4.dp`; target row `mergeTargetRowGap = 11.dp`, `mergeTargetRowPaddingVertical = 9.dp` (was sharing the Labels screen's own 16/13dp row metrics).

Everything the prototype doesn't depict (Labels screen rows, Recently Completed layout, Drawer) keeps its old-prototype measurements unchanged.

## Motion

Unchanged by this revert. Matches the prototype's CSS keyframes, except where noted:
- Quick Add card in/out: no dedicated transition yet (open/close is driven by state, not animated); the card itself appears/disappears with its parent recomposition.
- Toast in: slide up 20px + fade, ~200ms, same easing
- Backdrop fade: ~160ms ease-out
- **Drawer in/out, 220ms settle**: tap-driven open/close (hamburger, drawer item, scrim, back) animates a continuous open-progress from 0 (closed) to 1 (docked) via `Animatable` + `tween(220)`, owned in `ShoppApp.kt`. `DrawerMenu.kt` is a pure function of that `progress: Float` (panel `offset`, scrim alpha) rather than `AnimatedVisibility` on a boolean -- see the swipe entry below for why.
- **Swipe-right-to-open, tracks the finger 1:1** (List screen only): `ListScreen.kt`'s `detectHorizontalDragGestures` reports every incremental delta up to `ShoppApp`, which `snapTo`s the same `Animatable` progress driving the tap-driven animation above, so the panel tracks the touch point directly during the drag. On release, it settles to fully open or fully closed (whichever the drag passed the halfway point towards) with a quick 180ms `animateTo`.
- Row completion: 150ms height+opacity exit; reduced-motion becomes an instant removal, and the overlay fade drops its translation (PRD §11 accessibility).

## Components inventory

- **Row** (`ItemRow`): circular checkbox (22dp, border in `checkboxBorder`, fills `accent` mid-animation), single-line 16px regular text, optional trailing label tag (Recently Completed only).
- **Section header** (`SectionHeader`): small bold uppercase micro-label (12px, `.08em`) -- **not** the large 27px serif heading this used to be. **August 2026 (user request)**: no more leading identity dot -- the text itself is now tinted with the section's color (label color, or the fixed `inboxTint` for Inbox) instead of always `muted`. This resolves the "Known deliberate deviation" the previous revision of this doc flagged against PRD §11's "smaller/heavier" micro-header -- the new prototype turned out to specify exactly that.
- **Empty state** (`EmptyState`): a left-aligned 16px `muted` primary line (matching the prototype's single "Nothing on the belt." line exactly), plus a smaller secondary hint line underneath for product information the prototype's one-liner doesn't need to carry (e.g. how to reach Quick Add). Padding `34/22dp` top/horizontal, not the old hardcoded centered 120/40dp.
- **Chip** (`LabelChipRow`, Quick Add overlay only): pill (`chipCornerRadius = 100.dp`, `chipMinHeight = 28.dp`, 12.5px text). **August 2026 (user request), deviates from the prototype's uniform-accent chip**: unselected chips are `chipUnselectedFill`/`chipUnselectedText` (as before) plus a small leading dot (`chipDotSize = 7.dp`, `chipDotGap = 6.dp`) in the label's own palette color -- Inbox never gets a dot, it has no palette color. Selecting a chip promotes that identity color from the dot into the whole pill's fill (dot disappears, text switches to `chipSelected`/bold in a contrasting color) -- label chips use their palette color as fill with `chipSelectedText`; Inbox's selected fill is the inverted toast surface (`toastBackground`/`toastForeground` -- dark in light mode, cream in dark mode, since it has no palette color of its own to promote).
- **FAB**: `fabSize = 58.dp` circle, `accent` background, plus-icon only (no text label), offset `fabRightOffset = 22.dp` / `fabBottomOffset = 28.dp`.
- **Quick Add card** (`QuickAddOverlay`): compact floating card, **not** a full-width bottom sheet. Bottom-anchored, directly above the keyboard (`.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))`), width capped at `cardMaxWidth = 400dp` (irrelevant in practice at phone widths), rounded corners (`cardCornerRadius = 26dp`), `sheet` background (== `background`, see Color) with shadow-only elevation. Shared identically by the in-app FAB and `CaptureActivity` (Quick Settings tile / lock-screen capture).
- **Title-suggestions popover** (`TitleSuggestionsPopover`): autocomplete from purchase history, in the same popover slot as the `@label` suggestions popover above the Quick Add input, styled identically (`menu` background, `press` on the top row, 15px regular text) but with no colored dot. Shows while the line being typed isn't mid-`@token` and is ≥2 characters, matching distinct past titles from Recently Completed by prefix, most-recently-completed first. Tapping one replaces the line being typed with the full title.
- **Toast** (Undo): a **content-sized pill** (not a full-width bar), `toastBackground`/`toastForeground`, uppercase "Undo" action in `toastAction`, capped at `toastMaxWidth = 280.dp` as a defensive measure for an unusually long item title (the prototype's own pill never wraps, since it always shows just the fixed word "Undo" next to short text). Also used in Recently Completed to confirm re-adding an item (e.g. "Added to Groceries") with an Undo action that re-completes it; positioned bottom-center closer to the edge (`toastBottomOffsetNoFab`) since that screen has no FAB to clear.
- **Drawer**: left-anchored, fixed 290dp width, `menu` background, title in Caprasimo 26px. Slides in/out (see Motion). Opens via hamburger tap or a right-swipe anywhere on the List screen. Not depicted by the new prototype (no drawer mode in `ShoppApp.dc.html`) -- only its font family changed. **August 2026 (user request)**: bottom of the drawer now shows `Shopp {versionName} ({versionCode})` (`BuildConfig`, `footerNote` style, `muted`) pinned below the menu items via a `Spacer(Modifier.weight(1f))` -- moved here from the old "Shopp · 1.0" hardcoded line at the bottom of the Settings screen, which is now removed.
- **Label management sheet** (`LabelManagementSheet`): bottom sheet with Edit (rename + color), Merge, and Delete. **Edit**: rename field, plus (**August 2026, user request**) a color picker underneath -- swatches from the app's existing `labelPalette` (not a separate palette; a label's color is a single `colorIndex` into that one palette everywhere), wrapped 5 per row (`colorSwatchesPerRow`), `colorSwatchSize = 24dp` circle with a `colorSwatchRingSize = 32dp`/`colorSwatchRingWidth = 2dp` `foreground`-colored ring around the selected one. Tapping a swatch applies immediately via `onColorChange` (`SetLabelColor` use case) -- it doesn't wait for Save, which only commits the name. **Merge** shows an explicit radio circle per target label (`mergeRadioSize = 16dp`; outline in `line` when unselected, `accent`-filled with a `sheet`-colored inset ring when selected, matching the merge dialog's `box-shadow: inset 0 0 0 4px var(--color-surface)` look), target rows at `mergeTargetRowGap/PaddingVertical = 11/9.dp` in `mergeTargetLabel` (Figtree 15px regular -- distinct from the Labels screen's own 18px Light `labelName`), and requires picking a target then confirming. Its Cancel/Save/Merge/Delete-confirm actions use `dialogActionLabel` (Caprasimo) inside actual bordered/filled pill buttons (**August 2026, user request** -- previously bare adjacent text that read as too close together): Cancel is `line`-bordered/transparent, the confirm action is `accent`-filled with `onAccent` text, `sheetButtonGap = 14dp` apart, `sheetButtonCornerRadius = 100dp` pill, `sheetButtonPaddingHorizontal/Vertical = 18/9.dp`. This keeps the existing bottom-sheet container rather than switching to the prototype's centered modal card -- a scoped decision to avoid restructuring the whole component for one sub-mode; its background (`sheet`) and 32%-opacity scrim are consequently a small, accepted deviation from the merge dialog's own literal `--color-surface` card and 46% scrim. Slides up on open / down on close (`AnimatedVisibility` + `MutableTransitionState`, 220ms, matching the Drawer's duration) with a `HapticFeedbackType.LongPress` tick fired the moment the long-press is recognized in `LabelsScreen.kt`.
- **Settings theme control**: single pill-shaped segmented control (`ThemeSegmentedControl`/`ThemeSegment` in `SettingsScreen.kt`) -- one outer border, no gaps between the System/Light/Dark segments, selected segment `accent`-filled with SemiBold text (14px, Figtree -- literal; unselected stays Normal weight).
- **System bars**: `ShoppTheme` (`Theme.kt`) sets status/nav bar icon contrast (`WindowCompat.getInsetsController(...).isAppearanceLightStatusBars`/`isAppearanceLightNavigationBars`) every time the resolved `dark` flag changes, via a `SideEffect`. **August 2026 (user request)**: fixes light-mode icons rendering white-on-light (illegible) -- `enableEdgeToEdge()` in the Activity only sets this once at launch based on the *system* theme, which doesn't track the in-app `ThemeMode` override (Settings can force Light while the system is in Dark, or vice versa).
- **Toggle** (Settings): **August 2026 (user request)**: now Material3's native `Switch` (was a custom track+knob matching the prototype's exact 46×26dp/13dp-radius geometry) -- colors remapped onto `SwitchDefaults.colors()` (`accent` track/border + `onAccent` thumb when checked, `checkboxBorder` thumb/border + transparent track when unchecked) to keep the app's color scheme, but the shape/size now follows the platform switch, not the prototype.

## App icon

Adaptive icon (`res/drawable/ic_launcher_background.xml` + `ic_launcher_foreground.xml`): the app's single most-repeated motif, a circular checkbox + checkmark, enlarged to fill the icon (neither prototype ships an icon/favicon asset of its own). Background fill and the checkbox ring/checkmark stroke use the literal `--color-bg` (`#211E19`) / `--color-accent` (`#F6A06B`) dark-theme hex values directly -- **not** a live reference to `ShoppColors.background`, whose meaning changed in this pass (it's now neutral-100, `#2E2B25` in dark -- see Color); the icon was deliberately left on the darker, more saturated `--color-bg` tone as a brand color, matching what it already looked like. Static per-density `mipmap-*/ic_launcher*.webp` fallbacks weren't regenerated -- `minSdk` is 26, exactly the adaptive-icon minimum, so every supported device renders the vector layers, not those flattened fallbacks.

## Known deliberate deviations from the PRD / prototype

- **Quick Add card container**: compact floating card rather than a full-width bottom sheet -- but note the prototype's own card (edge-to-edge minus a 12dp margin) is much closer to this than the *old* prototype's true edge-to-edge sheet was, so this is a small deviation, arguably not one at all at phone widths.
- **Merge picker container**: kept as a mode within the existing bottom-sheet `LabelManagementSheet`, not split into the prototype's own centered modal card -- see Components inventory (also covers the resulting background/scrim opacity deviation).
- **Settings section label bottom padding**: shared (12dp) across both "Appearance"/"Theme" and "Behaviour"/"Labels" sections; the prototype specifies 12px for the first and 8px for the second. A 4dp deviation on the second section, not worth a third padding token.
- Drawer entrance is a full off-screen slide, not the old prototype's 16px micro-slide. Also opens via right-swipe in addition to the hamburger tap. Not depicted by the new prototype at all.
- **Label colors have a user-facing picker and a pastel palette**, both deviating from the prototype (which auto-allocates only, from its own literal 10-color earthy `PALETTE`). This flip-flopped twice in August 2026 -- added, reverted back to the prototype's literal behavior, then re-added at explicit user request with a new 15-color pastel palette; see History.
- The Undo toast's text is the item's full title as typed (no "Completed " prefix, no truncation to the first word) -- the prototype's own toast reads `Completed "<first word>"`. Left as product behavior rather than corrected as a visual-fidelity issue: truncating a multi-word title (e.g. "olive oil, 2L tin" → "olive") would be a usability regression, and the prefix/truncation is copy/behavior, not look.

## History

- **August 2026, color rebrand**: moved away from the prototype's warm palette to a neutral charcoal/white base with a bright red/yellow accent, and expanded the label palette to 15 colors with a user-facing picker.
- **August 2026, color revert**: both of the above undone at explicit user request, back to `ShoppApp.dc.html`'s warm cream/terracotta/olive palette and the prototype's flat 10-color auto-allocated label palette. Also picked up several shape deltas the (then-new) prototype export introduced that the app hadn't matched yet (checkbox size, FAB shape, Quick Add card metrics, Settings segmented control, merge picker radio buttons, uniformly-accent-filled chips). Light mode stayed a reconstruction, not a transcription, because the stylesheet with the real light tokens wasn't available yet.
- **August 2026, literal design-handoff pass**: `internal-docs/website/design_handoff_shopp_site/` arrived with the missing `organic-*/styles.css` (real light-mode tokens) and confirmed the app had never actually matched the prototype's fonts (Caprasimo/Figtree, not Newsreader/Roboto). This pass: (1) made both color themes literal transcriptions, including several alpha-based tokens (`muted`/`line`/`checkboxBorder`/`scrim`) that were previously solid-color guesses; (2) discovered `background` should be `--color-neutral-100` (the phone frame's own background), not `--color-bg` -- which also unifies `background` and `sheet` into one value per theme; (3) swapped the app's whole typeface from Newsreader/Roboto to self-hosted Caprasimo/Figtree; (4) replaced the List/Recently-Completed section header's large serif treatment with the prototype's small bold micro-label + identity dot; (5) corrected the empty state, undo toast (now a true content-sized pill), header row, and several other paddings/sizes the app hadn't matched from `ShoppApp.dc.html`; (6) added `chipUnselectedText`, `dialogActionLabel`, and `mergeTargetLabel` where a single shared token/style had been standing in for two literally-different prototype values.
- **August 2026, label color picker + pastel palette re-added (this document's current state)**: at explicit user request, re-added the manual color picker to `LabelManagementSheet`'s Edit view (`LabelRepository.setColor` / `SetLabelColor` use case, removed in the color revert above) and replaced the 10-color earthy `labelPalette` with a new 15-color pastel palette used everywhere a label's color is drawn -- auto-allocation, dots, section-header text, chip fills, and the picker itself. Same session also: dropped `SectionHeader`'s dot in favor of coloring the text directly; bumped `itemText`/`itemTextDone` to 18sp; swapped the Settings toggle for Material3's native `Switch`; moved the version string from a Settings footer to the Drawer; redesigned the Quick Add chip row (dot-when-unselected, full-color-fill-when-selected); fixed light-mode status/nav bar icon contrast; and turned the sheet's Cancel/Save/Merge/Delete-confirm text into real bordered/filled pill buttons.
