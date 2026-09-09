English | [简体中文](./CHANGELOG_ZH.md)

# Changelog

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## 27.2 (2026-09-09)

### Added

Expanded interface themes to 8 families and 11 variants. Alongside the existing Material You / Material Design 3 (Android 12–15) and miuix extension, this release adds:

| Theme family | Android versions and variants |
|---|---|
| Classic Android | Android 2.0 Eclair, Android 2.2 Froyo, and Android 2.3 Gingerbread |
| Honeycomb | Android 3.0–3.2 Honeycomb, based on Android 3.0 |
| Holo | Android 4.0 Ice Cream Sandwich with classic blue accents, and Android 4.4 KitKat with neutral accents |
| Material Design | Android 5.0–8.1, based on Android 5.0 Lollipop |
| Material Design 2 Rounded | Rounded Material Design 2 styling from the Android 9–11 era |
| Material Design 3 Expressive | Expressive styling associated with Android 16 and later |

- Added a dedicated Settings page for appearance, app lists, language display, and navigation.
- Added light, dark, and system display modes independent of the theme. Theme families and detail variants are listed chronologically with full names and descriptions, and each family remembers its last selection.
- Added persistent options for system font compatibility, system apps, package names, app types, regional flags, page swipes, double-tap-to-top, and the last visited page.
- Added horizontal body swiping across all five main pages with fixed headers and navigation, plus double-tap-to-top on the wide-screen navigation rail.
- Added universal, arm64-v8a, armeabi-v7a, x86, and x86_64 downloads for the official release.

### Improved

- Applied each theme's controls, typography, navigation, menus, and windows to the shared app-language, system-language, batch, and configuration workflows.
- Improved switch dragging, release, and cancellation feedback, and added slide transitions when switching between glass and standard bottom bars.
- Unified page insets, continuous lists, and section headings within each theme, with support for long names, larger fonts, and wide layouts.
- Shortened About while retaining requirements, the regional display statement, project links, and attribution; updated all four interface languages.

### Fixed

- Preserved unsaved system-language edits when initial loading completes or the page is revisited.

## 27.1 (2026-09-07)

### Added

- Added a System languages destination for viewing, adding, removing, reordering, and saving the device's global language list. Changes remain local to the editor until saved, and at least one language must remain.
- Added a native miuix interface alongside Material You, with separate theme backends and shared semantic colors, typography, and components.
- Added independent options for blurred top/bottom bars and a floating Liquid Glass bottom navigation bar.
- Added double-tap-to-top for the four bottom navigation tabs, including the glass bar, plus keyboard and accessibility equivalents. Scrolling does not refresh data or clear page state.
- Added per-application language changes from configuration details. A successful change automatically saves a new configuration while preserving the original; saving can be retried separately if the language changed but persistence failed.
- Added explicit System default targets to configurations, distinct from applications that a configuration does not manage. Version 2 JSON import/export preserves this distinction and accepts valid older configurations.
- Added focused JVM tests for tab double-tap detection, configuration comparison and copying, JSON compatibility, and configuration-edit failure recovery.

### Changed

- Consolidated the language directory across application details, batch operations, system-language editing, and configuration editing, with independent scroll state and predictive back transitions for nested language groups.
- Moved application actions and language search to the detail toolbar; the app icon and name appear in the title as the identity header scrolls out of view.
- Reworked application and language sort controls into a single row of chips that cycle through ascending, descending, and inactive states.
- Unified continuous-list alignment across compact and expanded layouts. Language and configuration selection backgrounds extend into the outer margin while foreground content retains its existing alignment.
- Replaced nested configuration-detail cards with compact summary actions and continuous application rows, including applications outside the saved configuration.
- Reorganized About into identity and introduction, app language, theme, core features, the regional-display statement, this project, and combined credits and references. The app-language sheet now uses the shared locale badges and choice rows.
- Added the One-China principle and regional-display statement to About and the bilingual READMEs.
- Replaced the fixed build identifier with an invocation-based `1B` sequence shared by all variants. Android version codes combine the marketing-version base with a sequence padded to at least three digits; unsigned variants retain their matching build-type fallbacks.

### Fixed

- Seeded the locale directory with the device's configured system languages so exact tags omitted from its runtime catalog can remain selectable.
- Removed duplicated horizontal sheet insets and corrected language-row spacing between selection backgrounds, badges, and trailing controls.
- Changed bottom-dock dismissal to slide downward and retain its glass backdrop until the exit finishes, avoiding a switch to the standard bar during dismissal.
- Prevented outgoing language-group content from accepting stale pointer, keyboard, or accessibility actions during transitions.
- Separated configuration application failures, uncertain results, and save failures, with pending-operation recovery and fixed-ID saves to avoid duplicate application or duplicate configurations on retry.

## 27.0 (2026-08-14)

Fuyao Locale is a substantial rewrite of the original Language Selector 1.04 baseline, preserving Android per-app locale management while rebuilding the product, architecture, and interface around current Android and Material 3 capabilities.

### Added

- Added multi-selection and batch locale application, including select-all-visible behavior, partial-failure reporting, system-application confirmation, and restoration to the system default.
- Added a reusable installed-application query model with search by name or package, modified-only filtering, system-application visibility, five sort modes, ascending or descending order, and pull-to-refresh.
- Added saved locale configurations that capture modified applications, compare saved and current states, report missing/current-only/changed entries, and apply a preset in one operation.
- Added portable JSON configuration import and export through Android's standard `OpenDocument` and `CreateDocument` APIs, including long-press export from a saved configuration.
- Added a three-destination Home, Configurations, and About structure with compact bottom navigation, wider-screen navigation rail, and expanded list-detail layouts.
- Added predictive back handling for search, multi-selection, sheets, language-group navigation, configuration detail, and expanded selection states.
- Added Fuyao Locale interface-language selection for English, Simplified Chinese, Japanese, and Brazilian Portuguese.
- Added localized locale names alongside autonyms, complete BCP 47 language/script/region variants, regional flag markers with letter fallbacks, locale search, and independent list scroll states.
- Added shared language-directory ordering. The recommended order keeps the target app's effective language first and uses locale-aware names from the current interface language; language groups and locale variants each provide five sort modes plus ascending or descending direction.
- Added explicit display of the device's effective locale for applications using System default.
- Added dedicated build identities for Release, Release Unsigned, Debug, and Debug Unsigned, plus private signing through an ignored `signing.properties` file.

### Changed

- Renamed the product from Language Selector to Fuyao Locale, changed the publication application ID from `vegabobo.languageselector` to `ing.fuyaoskyrocket.applocale`, and adopted version 27.0 with Build 1A569.
- Rebuilt the application around Kotlin 2.4.10, AGP 9.2.1, Compose BOM 2026.06.01, Material 3, Material 3 Adaptive, Hilt, KSP, Kotlin Flow, and Java 21.
- Replaced the original screen implementation with a token-based Material 3 design system, dynamic light/dark presentation, edge-to-edge gesture navigation, reusable list components, and adaptive compact/wide layouts.
- Consolidated application details, language groups, search results, and locale variants around one reusable language picker and one keyed lazy-list pipeline, including shared sorting state and locale-aware comparison.
- Simplified top app bars to text-only page titles while retaining navigation and contextual actions in their semantic slots.
- Reorganized package, locale, persistence, privileged-service, state, and UI responsibilities into focused models, data sources, repositories, use cases, ViewModels, and reusable composables.
- Changed locale loading to build and cache grouped runtime locale data outside list items, while deriving useful language, script, region, and script-region aliases from Android's available locales.
- Changed application loading to publish the package list first, fetch locale tags in one Binder batch, cache the completed snapshot across configuration changes, and update only affected rows after locale operations.
- Replaced the original About implementation with project-focused bilingual content, app-language controls, the Fuyao Locale repository link, the original VegaBobo project reference, and the applicable AGPL-3.0-only and Apache-2.0 notices.
- Licensed Fuyao Locale as a whole under AGPL-3.0-only while retaining the upstream Apache-2.0 license and attribution for Language Selector-derived portions.
- Updated the application, adaptive, monochrome, and Quick Settings tile branding for Fuyao Locale.
