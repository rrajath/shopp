# Shopp

A fast, native Android shopping list app. Type an item, tag it with `@label` to route it into a section, tap to check it off, and add items from the lock screen through a Quick Settings tile without unlocking the phone.

## Purpose

Shopp implements the product requirements in `internal-docs/shopping-list-prd.md` and the technical design in `internal-docs/shopping-list-tdd.md`, adapted to a pure native Android stack (see [Architecture](docs/ARCHITECTURE.md) for why). The visual design and screen scope follow the prototype at `internal-docs/design/Shopp Prototype.dc.html`, which takes precedence over the PRD where the two disagree.

## The problem it solves

Shopping lists fail at the moment they're needed most: standing in an aisle, or remembering something the second before you fall asleep. Shopp is built around that moment specifically:

- **Capture has to be instant.** A shared Quick Settings tile opens a translucent capture surface over whatever's on screen — including the lock screen — writes directly to the same on-device database the main app reads, and gets out of the way.
- **Organizing can't cost extra taps.** Typing `@label` inline while capturing an item files it into a section immediately; there's no separate "assign a category" step.
- **Undo has to be trustworthy.** Checking an item off is instant and real (not optimistic-then-maybe-fails), but a 4-second undo window means a mis-tap never costs you the item.

## Features

- **Quick Add** — a bottom-sheet capture surface with autofocus, multi-line paste support, inline `@label` tagging with autocomplete, a sticky label chip that persists across consecutive adds, and a small "just added" list for confidence.
- **Quick Settings tile** — add items without unlocking the phone or opening the app; the tile launches a translucent activity over the lock screen that shares the exact same capture component and database as the in-app flow.
- **Sectioned list** — items group by label (Inbox always shown first), with sticky section headers while scrolling; grouping can be turned off in Settings for one flat list.
- **Tap-to-complete with undo** — completing an item is a real, immediate write; a toast with a 4-second undo window follows, and a new completion during that window commits the previous one and starts a fresh timer.
- **Inline edit** — tap an item's title to edit it in place, with the text caret placed at the exact point you tapped.
- **URL linkification** — a URL typed into a title becomes a tappable link without entering edit mode.
- **Recently Completed** — a reverse-chronological log of completed items, grouped by day, with tap-to-re-add and a Clear action (optionally confirmed).
- **Labels management** — rename, merge, or delete labels via a long-press sheet; deleting a label moves its items to Inbox rather than deleting them, merging reassigns items then removes the source label, both atomically.
- **Settings** — System/Light/Dark appearance, and three behavior toggles (group by label, keep Quick Add open after submit, confirm before clearing Recently Completed).

## Setup

**Requirements**

- Android Studio with an Android SDK including platform 36 and build-tools for it
- JDK 21
- An emulator or device running Android 8.0 (API 26) or newer

**Build and run**

```sh
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration.

**Run tests**

```sh
./gradlew :app:testDebugUnitTest
```

**Try the Quick Settings tile**

Pull down the notification shade twice to reach Quick Settings, tap the pencil/edit icon, and drag the "Add to Shopp" tile into your active tiles. Long-pressing (or tapping, depending on Android version) it opens the capture sheet without unlocking the phone.

## Project layout

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full package breakdown and the reasoning behind key decisions (Room over raw SQLite, manual DI instead of Hilt, why there's no cross-language "parser kernel," etc.), and [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md) for the color/type/spacing tokens and component inventory.

## Status

All 11 implementation milestones are complete — see `PROGRESS.md` (gitignored, local-only) for the detailed build log, bugs found and fixed along the way, and testing notes.
