English | [简体中文](./CHANGELOG_ZH.md)

# Changelog

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

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
