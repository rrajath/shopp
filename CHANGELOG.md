# Changelog

- [How versions and releases work](#how-versions-and-releases-work)
- [Unreleased](#unreleased), followed by each released version, newest first

All notable user-facing changes to Shopp are documented in this file. The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

### How versions and releases work

`VERSION_NAME` in `gradle.properties` is the single source of truth. It's a SemVer string (`MAJOR.MINOR.PATCH`): major = breaking change, minor = new feature, patch = fix or tweak. `VERSION_CODE`, stored next to it, must equal `MAJOR*10000 + MINOR*100 + PATCH` (so `0.4.0` is `400`; keep minor and patch each within 0-99), and the build fails if it doesn't. CI never computes or bumps either value.

Add each user-facing change under `## [Unreleased]` as you go, under Added, Changed, or Fixed. To cut a release, run `scripts/release.sh 0.4.0` locally. It runs the unit tests, bumps both version keys, archives `## [Unreleased]` as `## [0.4.0] - <UTC date>` under a fresh empty `## [Unreleased]`, commits both as `Release v0.4.0`, tags that commit `v0.4.0`, and moves `main` to it. Then push `main` and the tag (the script prints both commands). The tag push builds signed APKs and publishes a GitHub Release, using the matching section of this file as the release notes.

CI never edits this file or the version keys, so the tagged commit is exactly what gets built. Releases up to v0.3.0 predate this file; their notes are on [GitHub Releases](https://github.com/rrajath/shopp/releases).

## [Unreleased]

### Added
- Set an item's quantity with a -/+ stepper in Quick Add and inline edit; quantities above 1 show as "Title xN".
- Quick Add shows Frequent and Recent tabs while empty; tap a past purchase to add it under its last label.
- Title suggestions show their label's color dot, and picking one files the item under the label it was last bought with.

### Changed
- Adding an item already on the list keeps Quick Add open and shows a hint instead of silently dropping it.
- Dismissing the keyboard ends inline edit and saves the change, like tapping Done.

### Fixed
- Tapping an item in the widget completes it again in release builds.
- The keyboard no longer covers the row you're editing inline.
- The debug build's widget shows "Shopp Debug" as its name.
