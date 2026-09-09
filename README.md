<p align="center">
  English | <a href="README_ZH.md">简体中文</a>
</p>

<h1 align="center">Fuyao Locale</h1>

<p align="center">
  A companion for viewing, managing, and preserving Android per-app locales
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-13%2B-3DDC84?logo=android&amp;logoColor=white" alt="Android 13 or later">
  <img src="https://img.shields.io/badge/version-27.2-4F6B00" alt="Version 27.2">
  <img src="https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&amp;logoColor=white" alt="Kotlin 2.4.10">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-AGPL--3.0--only-blue" alt="AGPL-3.0-only license"></a>
</p>

Fuyao Locale provides a focused interface for Android's per-app language capability on Android 13 and later. It is intended for devices whose firmware does not expose a suitable application-language manager, while also providing batch operations and reusable locale configurations that the system interface normally lacks.

The app does not translate other applications. It asks Android to apply a locale that the selected application already supports.

## Statement on the One-China principle and regional display / 关于一个中国原则及地区显示的声明

Fuyao Locale and its developer uphold the One-China principle. If references to the Taiwan region or associated flag symbols appear in the app, the regional names are supplied by Android's locale data, while flag symbols are generated from region codes using standard Unicode sequences and rendered by the device's system fonts. These names and flag graphics are not individually authored or designed by the developer for that region. Their appearance reflects the device's system data and rendering support and does not imply any change in, or departure from, this position.

Fuyao Locale 及其开发者坚持一个中国原则。应用中如出现台湾地区相关名称或旗帜符号，其地区名称来源于 Android 系统的区域设置数据，旗帜符号则由通用逻辑依据地区代码生成标准 Unicode 字符序列，并由设备系统字库渲染，并非开发者针对该地区单独编写名称或设计、绘制旗帜图案。上述显示仅反映设备系统的数据及渲染支持情况，不代表本应用及开发者立场的改变或偏离。

## Project lineage

Fuyao Locale is a substantial Kotlin and Jetpack Compose rewrite based on the concepts and functionality of [VegaBobo/Language-Selector](https://github.com/VegaBobo/Language-Selector). It preserves the original project's purpose—managing Android per-app locales through a privileged service—while replacing the product identity, architecture, interface, list pipeline, and configuration workflow.

The original project remains credited in the app and this repository. Fuyao Locale is licensed as a whole under the [GNU Affero General Public License v3 only](LICENSE) (`AGPL-3.0-only`). Portions derived from Language Selector retain their Apache-2.0 notices; the upstream license is preserved at [LICENSES/Apache-2.0.txt](LICENSES/Apache-2.0.txt), with project lineage documented in [NOTICE](NOTICE).

## Features

### Application locale management

- View installed user and system applications with their application name, package name, modification state, and effective locale.
- Resolve **System default** to the device's actual current locale instead of displaying an unspecified state.
- Set a locale for one application or restore it to the system default.
- Long-press applications to enter multi-selection mode, select all visible results, and apply one locale to the batch.
- Open, force-stop, or open the system settings page for an application from its detail screen.
- Refresh only affected rows after locale changes so the home list stays current.

### Search, filtering, and ordering

- Search directly inside the primary application list by application name or package name.
- Filter the list to applications with a non-default locale.
- Show or hide system applications; the selection is saved and kept in sync with Settings. System applications are shown by default.
- Sort by application name, package name, locale, modification state, or application type, in ascending or descending order.
- Pull to refresh the installed-application and locale snapshot.

### Language directory

- Build the locale directory at runtime from Android's `Locale.getAvailableLocales()` data.
- Group locales by language and expose language, script, region, and script-region BCP 47 variants such as `zh`, `zh-CN`, `zh-Hans`, and `zh-Hans-CN` when supported by the system data.
- Display names in both the current Fuyao Locale interface language and each language's autonym.
- In the default order, keep the target app's effective language first and sort the remaining language groups by their names in the current Fuyao Locale interface language. System-default apps use the device's effective system locale.
- Sort language groups by recommended order, interface-language name, autonym, language tag, or variant count. Sort locale variants and search results by recommended order, interface-language name, autonym, language tag, or tag specificity; both levels support ascending and descending order.
- Optionally show regional flags through the system emoji renderer. Turn flags off to show region or script codes; language/script markers remain the fallback when a flag is unavailable.
- Search the locale directory, pin frequently used locales, and cycle pinned locales from the Quick Settings tile.
- Keep the directory, search results, and opened variant group on independent scroll states; changing a sort order returns only the active list to its start.

The available locale catalog follows the Android runtime on the device. A future system update can therefore add or adjust locale data without requiring a bundled language database update in Fuyao Locale.

### Saved configurations

- Save the current set of applications with non-default locales as a reusable configuration.
- Compare a configuration with the device before applying it. The difference view distinguishes changed locales, missing applications, and current modifications outside the saved configuration.
- Apply, refresh, or delete a saved configuration.
- Import and export the app's portable JSON configuration format through Android's standard system file picker.
- Long-press a saved configuration to export it with the standard `CreateDocument` flow.
- Use a dedicated detail route on compact screens and a list-detail layout on expanded screens.

### Interface themes and adaptive behavior

- Eight theme families: Classic Android (Android 2.0–2.3), Honeycomb (Android 3.0–3.2), Holo (Android 4.0–4.4), Material Design (Android 5.0–8.1), Material Design 2 Rounded (Android 9–11), Material You / Material Design 3 (Android 12–15), Material Design 3 Expressive (Android 16+), and the miuix extension (this app runs on Android 13+). Classic Android defaults to Gingerbread and retains Eclair/Froyo; Holo retains ICS blue and KitKat neutral variants, for 11 stable variants in total.
- Material uses fixed primary colors, paper shadows, and first-generation ripples. Material Rounded uses native Material 2 with neutral surfaces and a fixed blue accent; Material You uses wallpaper colors and standard Material 3. Expressive adds spring motion, emphasized type, morphing buttons, loading indicators, and short navigation. See [Material generations](docs/android-themes/MATERIAL_GENERATIONS.md).
- Independent light, dark, and system display modes. Theme families and detail variants have separate, chronologically ordered pickers with full names and Android versions. Existing selections are preserved, and each family remembers its last variant. Retro themes use versioned framework resources. System fonts are used by default for Chinese glyphs and font weights, with an option to restore the original era fonts.
- Settings groups appearance, app lists, language display, and navigation, with saved options for system apps, package names, app types, regional flags, page swipes, double-tap-to-top, and the last visited page. Retro themes retain saved modern effect preferences while disabling those effects.
- Five destinations: Apps, System languages, Configurations, Settings, and About. Modern themes use bottom navigation on compact screens and a navigation rail on wider windows; retro themes use their era-specific top tabs.
- Drag page bodies horizontally with HorizontalPager while headers, tabs, and bottom or side navigation remain fixed. Each page retains its own search, filters, scroll positions, and drafts; detail routes keep back navigation. Single taps switch pages immediately, and double taps scroll to the top, with keyboard and accessibility equivalents.
- KitKat uses white selection indicators and neutral highlights. Classic 2.x tabs use the original amber focus artwork; Froyo retains its 2.2 dropdown states and progress resources.
- List-detail layouts for expanded windows, with width constraints for readable content.
- Era-specific title bars, search, menus, and selection controls retain the same application operations.
- A single keyed lazy list for application details and locale rows, with theme-specific spacing and support for long labels.
- Edge-to-edge drawing for gesture navigation, including HyperOS devices.
- Predictive back handling for navigation, search, multi-selection, sheets, nested language groups, and wide-screen detail state.
- Interface language selection for English, Simplified Chinese, Japanese, and Brazilian Portuguese.

Source provenance, per-theme implementation coverage, and remaining device/reference checks are recorded in [Android theme validation](docs/android-themes/ACCEPTANCE.md). Build results do not establish pixel-level equivalence with historical Android systems.

## Requirements

- Android 13 (API 33) or later.
- [Shizuku](https://shizuku.rikka.app/) installed, running, and authorized for Fuyao Locale. Shizuku is the currently supported user-facing privilege path.
- A target application that actually supports the locale you choose.

Changing a system application or selecting a locale unsupported by the target application may cause unexpected behavior. Fuyao Locale intentionally keeps system applications available, but shows a confirmation before batch operations that include them.

## Usage

1. Install and start Shizuku.
2. Open Fuyao Locale and explicitly request Shizuku permission.
3. Search, filter, or sort the application list to locate a target.
4. Tap one application, then search or sort the language directory and choose a locale. Long-press applications on Home to apply one locale to a batch.
5. Open **Configurations** to save the current modified-app snapshot, import a JSON configuration, inspect differences, or apply a saved configuration.
6. Pin common locales in the language picker if you want to cycle them through the Quick Settings tile.

Permission prompts are only launched from an explicit user action. Rotation and Fuyao Locale's own interface-language changes reuse the process cache and privileged connection instead of requesting permission or rescanning all applications again.

## Configuration file format

Imports and exports use the same portable JSON array. `localeTag` is a BCP 47 language tag, `createdAt` is a Unix timestamp in milliseconds, and matching configuration IDs replace older local copies when imported.

~~~json
[
  {
    "id": "example-configuration-id",
    "createdAt": 1786608000000,
    "entries": [
      {
        "packageName": "com.example.app",
        "label": "Example App",
        "localeTag": "zh-CN"
      }
    ]
  }
]
~~~

Each imported configuration must have a non-empty `id`, a positive `createdAt`, and at least one valid entry. Duplicate package entries within one configuration are collapsed by package name.

## Build and install

Use JDK 21 and an Android SDK containing Platform 37.

~~~bash
./gradlew :app:assembleRelease
~~~

Release APKs are written to `app/build/outputs/apk/release/`. All four build types use this filename format:

~~~text
FuyaoLocale-<applicationId>-<marketingVersion>(<buildNumber>)-<abi>-<variant>.apk
~~~

The version segment combines the marketing version and build identifier, for example `27.2(1C82)`. Android versionName and versionCode remain in the APK and build metadata instead of being repeated in its filename. Each build type produces five APKs: one `universal` containing all four ABIs, plus separate `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64` packages. Field separators are hyphens; ABI names retain their official spelling, including `x86_64`. The variant is `release`, `debug`, `releaseUnsigned`, or `debugUnsigned`, and the application ID includes that variant's suffix. Building all four types produces 20 APKs.


The 27.2 build train is `1C`. Its sequence counts build invocations after the 27.1 submission, whose cumulative counter was 36: cumulative build 118 becomes `1C82`, with Android versionCode `272082`. The ignored root `.build-counter` remains cumulative; changing the train does not erase it. All variants in one invocation share a sequence, and failed builds still consume their reserved number.

Use the generated metadata to install the current universal Release APK on a connected device:

~~~bash
FUYAO_LOCALE_APK="$(python3 -c 'import json,pathlib; p=pathlib.Path("app/build/outputs/apk/release"); m=json.loads((p/"output-metadata.json").read_text()); print(p/next(e["outputFile"] for e in m["elements"] if not e.get("filters")))')"
adb install -r "$FUYAO_LOCALE_APK"
~~~

Run these commands from the repository root after a successful build. Metadata selects the current output even if older APKs remain in the directory. If metadata is missing, complete the Release build first; a successful install prints `Success`. The `-r` option preserves existing app data. No cleanup is required for this shell variable.

### Signing and build variants

Copy `signing.properties.example` to the ignored project-root file `signing.properties`, then provide your own `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`. When all four values are present, the canonical Release build uses that private key. Otherwise, it falls back to the local debug key only to keep local installation available; do not distribute that fallback artifact.

| Variant | Application ID | App label | Task | Intended use |
| :--- | :--- | :--- | :--- | :--- |
| Release | `ing.fuyaoskyrocket.applocale` | Fuyao Locale | `:app:installRelease` | Canonical installable and publishable build when privately signed |
| Release Unsigned | `ing.fuyaoskyrocket.applocale.unsigned` | Fuyao Locale Release Unsigned | `:app:assembleReleaseUnsigned` | Inspectable unsigned release output |
| Debug | `ing.fuyaoskyrocket.applocale.debug` | Fuyao Locale Debug | `:app:installDebug` | Development installation |
| Debug Unsigned | `ing.fuyaoskyrocket.applocale.debug.unsigned` | Fuyao Locale Debug Unsigned | `:app:assembleDebugUnsigned` | Inspectable unsigned development output |

Unsigned APKs cannot be installed directly.

## Technology

| Area | Main technologies |
| :--- | :---------------- |
| Language and build | Kotlin 2.4.10, Java 21, AGP 9.2.1, KSP |
| UI | Jetpack Compose, Material 3, Material 3 Adaptive, edge-to-edge system bars |
| State and navigation | ViewModel, Kotlin Flow, Navigation Compose, predictive back |
| Dependency injection | Hilt |
| Privileged bridge | Shizuku user service, AIDL, hidden API stubs |
| Local persistence | SharedPreferences-backed pinned locales and saved JSON configurations |

## Architecture and performance

~~~text
PackageManager / Android locale services
                  │
                  ▼
 PackageDataSource + PrivilegedLocaleDataSource
                  │
                  ▼
 Repositories + focused application/configuration use cases
                  │
                  ▼
       Hilt ViewModels + immutable UI state
                  │
                  ▼
       Reusable Compose Material 3 components
~~~

The rewrite keeps privileged Binder operations outside composables. Installed applications are published before locale metadata, locale tags are fetched in one batch Binder call, and the completed snapshot is cached across Activity recreation. Search, filtering, and stable sorting run before keyed `LazyColumn` items are composed. Locale ordering is centralized in the shared picker and uses a locale-aware `Collator` for the current Fuyao Locale interface language. Application icons use a bounded memory cache, while locale changes update only affected rows.

## Project layout

~~~text
app/
├── src/main/aidl/           Privileged service contract
├── src/main/java/.../
│   ├── data/                Local stores, system data sources, repositories, use cases
│   ├── model/               Immutable application, locale, query, and configuration models
│   ├── service/             Privileged service bridge and AIDL implementation
│   └── ui/                  Material 3 screens, reusable components, and design tokens
└── src/main/res/            Localized strings, launcher assets, XML theme, and metadata
hidden_api/                  Compile-only Android hidden API stubs
gradle/                      Version catalog and Gradle Wrapper configuration
signing.properties.example   Private-signing configuration template
README.md                    English documentation
README_ZH.md                 Simplified Chinese documentation
CHANGELOG.md                 English release history
CHANGELOG_ZH.md              Simplified Chinese release history
LICENSE                      GNU AGPL v3 license text
LICENSES/Apache-2.0.txt      Retained upstream Apache-2.0 license
NOTICE                       Project lineage and modification notice
~~~

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for the release-level differences from the original Language Selector baseline.

## License

Fuyao Locale is licensed under the [GNU Affero General Public License v3 only](LICENSE) (`AGPL-3.0-only`). Code derived from Language Selector remains subject to its retained Apache-2.0 notices; see [LICENSES/Apache-2.0.txt](LICENSES/Apache-2.0.txt) and [NOTICE](NOTICE).
