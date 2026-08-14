<p align="center">
  <a href="README.md">English</a> | 简体中文
</p>

<h1 align="center">Fuyao Locale</h1>

<p align="center">
  用于查看、管理和保存 Android 应用独立语言配置的 Material 3 工具
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-13%2B-3DDC84?logo=android&amp;logoColor=white" alt="Android 13 或更高版本">
  <img src="https://img.shields.io/badge/version-27.0-4F6B00" alt="版本 27.0">
  <img src="https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&amp;logoColor=white" alt="Kotlin 2.4.10">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-AGPL--3.0--only-blue" alt="AGPL-3.0-only 许可证"></a>
</p>

Fuyao Locale 面向 Android 13 及以上版本，为系统应用独立语言能力提供集中管理界面。它既适用于固件没有提供合适“应用语言”入口的设备，也补充了系统界面通常缺少的批量操作和可复用语言配置。

本应用不会翻译其他应用，而是请求 Android 为目标应用应用其已经支持的语言。

当前版本：**27.0** · Build **1A569** · 应用 ID **`ing.fuyaoskyrocket.applocale`**

## 项目脉络

Fuyao Locale 是基于 [VegaBobo/Language-Selector](https://github.com/VegaBobo/Language-Selector) 思路和功能进行的 Kotlin 与 Jetpack Compose 大幅重写。项目保留了通过特权服务管理 Android 应用独立语言的核心用途，同时重新构建了产品标识、代码架构、界面、列表数据流和配置保存流程。

应用内和仓库中均保留原项目署名。Fuyao Locale 整体采用 [GNU Affero General Public License v3 only](LICENSE)（`AGPL-3.0-only`）。源自 Language Selector 的部分继续保留 Apache-2.0 许可证与署名；上游许可证存放于 [LICENSES/Apache-2.0.txt](LICENSES/Apache-2.0.txt)，项目来源与修改说明见 [NOTICE](NOTICE)。

## 核心功能

### 应用语言管理

- 展示已安装的用户应用和系统应用，包括应用名称、包名、修改状态和实际 Locale。
- 将“系统默认”解析为设备当前使用的真实 Locale，而不是展示不明确的空状态。
- 为单个应用设置语言，或恢复为系统默认语言。
- 长按应用进入多选模式，可选择当前可见的全部结果，并批量应用同一种语言。
- 在应用详情页打开应用、强制停止应用或进入系统应用设置页。
- 语言修改后仅刷新受影响的列表项目，保证返回首页后状态及时更新。

### 搜索、筛选和排序

- 按应用名称或包名直接筛选首页主列表，不创建独立的搜索结果列表。
- 仅展示已设置非默认语言的应用。
- 显示或隐藏系统应用；系统应用默认展示。
- 支持按应用名称、包名、Locale、修改状态和应用类型排序，并可切换升序或降序。
- 通过下拉刷新重新读取应用和 Locale 快照。

### 语言目录

- 运行时从 Android 的 `Locale.getAvailableLocales()` 构建语言目录。
- 按语言分组，并根据系统数据补全语言、书写系统、地区及书写系统—地区 BCP 47 变体，例如 `zh`、`zh-CN`、`zh-Hans` 和 `zh-Hans-CN`。
- 同时展示当前 Fuyao Locale 界面语言中的名称和语言自称。
- 默认将目标应用当前实际使用的语言置顶，其余语言按当前 Fuyao Locale 界面语言中的名称排序；使用“系统默认”的应用以设备当前系统 Locale 作为实际语言。
- 语言组支持按推荐顺序、界面语言名称、语言自称、语言标签和变体数量排序；语言变体及搜索结果支持按推荐顺序、界面语言名称、语言自称、语言标签和标签层级排序，两级列表均可切换升序或降序。
- 通过系统 Emoji 渲染器展示地区国旗；没有对应国旗时，完整显示两位或三位语言/书写系统字母标识。
- 支持搜索语言目录、置顶常用语言，并通过快捷设置磁贴循环切换置顶语言。
- 语言目录、搜索结果和语言变体子列表分别保存独立的滚动位置；切换排序方式时只将当前列表返回起始位置。

可用语言目录跟随设备 Android 运行时数据。未来系统更新加入或调整 Locale 后，Fuyao Locale 无需更新内置语言数据库即可读取对应变化。

### 配置保存

- 将当前所有使用非默认 Locale 的应用保存为可复用配置。
- 应用前对比配置与设备现状，区分需要修改、应用缺失以及仅存在于当前设备的额外修改。
- 支持应用、刷新或删除已保存配置。
- 通过 Android 标准系统文件选择器导入和导出应用原生 JSON 配置格式。
- 长按已保存配置，通过标准 `CreateDocument` 流程导出文件。
- 紧凑屏幕使用独立详情页，宽屏使用列表—详情布局。

### Material 3 与自适应交互

- 使用原生 Material 3 页面、组件、语义颜色、字体层级和明暗主题。
- 首页、配置和关于三个一级页面：紧凑屏幕使用底部导航，宽屏使用 Navigation Rail。
- 展开窗口使用列表—详情布局，并限制正文最大宽度以保证可读性。
- 页面顶部仅展示文本标题，返回和上下文操作保留在 Top App Bar 对应语义位置。
- 应用详情和语言项目使用同一个带稳定 key 的惰性列表，复用统一对齐 Token；过长标签使用自动滚动展示。
- 支持包括 HyperOS 在内的全面屏手势和 Edge-to-Edge 绘制。
- 为页面导航、搜索、多选、Bottom Sheet、语言变体层级和宽屏详情状态提供预测性返回。
- Fuyao Locale 自身支持英文、简体中文、日文和巴西葡萄牙文界面。

## 使用条件

- Android 13（API 33）或更高版本。
- 已安装并启动 [Shizuku](https://shizuku.rikka.app/)，且已向 Fuyao Locale 授权。当前面向用户提供的特权访问方式为 Shizuku。
- 目标应用实际支持所选语言。

修改系统应用，或为目标应用选择其不支持的语言，可能产生不可预期的结果。Fuyao Locale 保留系统应用操作能力，但批量选择中包含系统应用时会先显示确认提示。

## 使用方式

1. 安装并启动 Shizuku。
2. 打开 Fuyao Locale，主动点击请求 Shizuku 权限。
3. 通过搜索、筛选或排序找到目标应用。
4. 点击单个应用后搜索或排序语言目录并选择 Locale；也可以在首页长按应用，为多个应用批量设置同一种语言。
5. 打开“配置”，保存当前已修改应用、导入 JSON 配置、查看差异或一键应用已保存配置。
6. 在语言选择器中置顶常用语言，需要时通过快捷设置磁贴循环切换。

权限窗口只会由用户主动操作触发。横竖屏切换和 Fuyao Locale 自身界面语言切换会复用进程缓存和特权连接，不会重新请求权限或重新扫描全部应用。

## 配置文件格式

导入和导出使用同一种可移植 JSON 数组格式。`localeTag` 为 BCP 47 语言标签，`createdAt` 为毫秒级 Unix 时间戳；导入时，相同配置 ID 会替换本地旧版本。

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

每个导入配置必须包含非空 `id`、大于零的 `createdAt` 和至少一个有效应用项目。同一配置内重复的包名会按包名去重。

## 构建与安装

请使用 JDK 21，以及包含 Platform 37 的 Android SDK。

~~~bash
./gradlew :app:assembleRelease
~~~

Release APK 输出路径：

~~~text
app/build/outputs/apk/release/app-release.apk
~~~

安装到已连接设备：

~~~bash
adb install -r app/build/outputs/apk/release/app-release.apk
~~~

### 签名与构建变体

将 `signing.properties.example` 复制为项目根目录下已被 Git 忽略的 `signing.properties`，再填写自己的 `storeFile`、`storePassword`、`keyAlias` 与 `keyPassword`。四项均存在时，标准 Release 会使用私钥签名；否则仅为保持本机安装可用而回退至 debug 密钥，不应分发该回退产物。

| 变体 | 应用 ID | 应用名称 | 任务 | 用途 |
| :--- | :--- | :--- | :--- | :--- |
| Release | `ing.fuyaoskyrocket.applocale` | Fuyao Locale | `:app:installRelease` | 配置私钥后用于标准安装和发布 |
| Release Unsigned | `ing.fuyaoskyrocket.applocale.unsigned` | Fuyao Locale Release Unsigned | `:app:assembleReleaseUnsigned` | 用于检查的未签名 Release 产物 |
| Debug | `ing.fuyaoskyrocket.applocale.debug` | Fuyao Locale Debug | `:app:installDebug` | 开发安装 |
| Debug Unsigned | `ing.fuyaoskyrocket.applocale.debug.unsigned` | Fuyao Locale Debug Unsigned | `:app:assembleDebugUnsigned` | 用于检查的未签名开发产物 |

未签名 APK 不能直接安装。

## 技术栈

| 范围 | 主要技术 |
| :--- | :------- |
| 语言与构建 | Kotlin 2.4.10、Java 21、AGP 9.2.1、KSP |
| UI | Jetpack Compose、Material 3、Material 3 Adaptive、Edge-to-Edge 系统栏 |
| 状态与导航 | ViewModel、Kotlin Flow、Navigation Compose、预测性返回 |
| 依赖注入 | Hilt |
| 特权桥接 | Shizuku UserService、AIDL、hidden API stubs |
| 本地存储 | 基于 SharedPreferences 的置顶语言和 JSON 配置存储 |

## 架构与性能

~~~text
PackageManager / Android Locale 服务
                  │
                  ▼
 PackageDataSource + PrivilegedLocaleDataSource
                  │
                  ▼
       Repository + 聚焦的应用/配置用例
                  │
                  ▼
       Hilt ViewModel + 不可变 UI 状态
                  │
                  ▼
       可复用 Compose Material 3 组件
~~~

重写后的实现不会在 Composable 中执行特权 Binder 操作。应用基础列表先行展示，随后通过单次批量 Binder 调用读取 Locale，并在 Activity 重建时复用完整进程缓存。搜索、筛选和稳定排序在带 key 的 `LazyColumn` 组合前完成。语言排序集中在共享选择器中，并使用与当前 Fuyao Locale 界面语言对应的 `Collator`；应用图标使用有界内存缓存，语言操作只更新受影响的项目。

## 项目结构

~~~text
app/
├── src/main/aidl/           特权服务接口
├── src/main/java/.../
│   ├── data/                本地存储、系统数据源、Repository 与用例
│   ├── model/               应用、Locale、查询与配置不可变模型
│   ├── service/             特权服务桥接与 AIDL 实现
│   └── ui/                  Material 3 页面、可复用组件与设计 Token
└── src/main/res/            多语言文本、启动图标、XML 主题与元数据
hidden_api/                  仅编译期使用的 Android hidden API stubs
gradle/                      Version Catalog 与 Gradle Wrapper 配置
signing.properties.example   私钥签名配置模板
README.md                    英文文档
README_ZH.md                 简体中文文档
CHANGELOG.md                 英文更新日志
CHANGELOG_ZH.md              简体中文更新日志
LICENSE                      GNU AGPL v3 许可证全文
LICENSES/Apache-2.0.txt      保留的上游 Apache-2.0 许可证
NOTICE                       项目来源与修改说明
~~~

## 更新日志

版本级变化请参阅 [CHANGELOG_ZH.md](CHANGELOG_ZH.md)。该日志只汇总当前版本相对最初 Language Selector 基线的最终差异。

## 许可证

Fuyao Locale 采用 [GNU Affero General Public License v3 only](LICENSE)（`AGPL-3.0-only`）。源自 Language Selector 的代码仍须保留其 Apache-2.0 许可证与署名，详见 [LICENSES/Apache-2.0.txt](LICENSES/Apache-2.0.txt) 和 [NOTICE](NOTICE)。
