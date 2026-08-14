[English](./CHANGELOG.md) | 简体中文

# 更新日志

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)。

## 27.0 (2026-08-14)

Fuyao Locale 以最初的 Language Selector 1.04 为基线进行了大幅重写，在保留 Android 应用独立语言管理能力的同时，按照当前 Android 与 Material 3 能力重新构建产品功能、代码架构和界面。

### 新增

- 增加应用多选和批量语言设置，支持选择当前全部可见应用、批量恢复系统默认、部分失败结果提示以及系统应用确认。
- 增加可复用的应用列表查询模型，支持按名称或包名搜索、仅看已修改、显示/隐藏系统应用、五种排序方式、升降序切换和下拉刷新。
- 增加语言配置保存，可记录当前已修改应用，对比保存状态与设备现状，区分应用缺失、当前额外修改和需要应用的差异，并支持一键应用。
- 增加通过 Android 标准 `OpenDocument` 和 `CreateDocument` API 导入、导出通用 JSON 配置；长按已保存配置可直接进入导出流程。
- 增加首页、配置、关于三个一级页面，紧凑屏幕使用底部导航，宽屏使用 Navigation Rail，展开屏幕使用列表—详情布局。
- 为搜索、多选、Bottom Sheet、语言分组、配置详情和宽屏选择状态增加预测性返回。
- 增加 Fuyao Locale 自身界面语言选择，支持英文、简体中文、日文和巴西葡萄牙文。
- 增加当前界面语言名称与语言自称并列展示、完整 BCP 47 语言/书写系统/地区变体、地区国旗与字母回退、语言搜索及独立列表滚动状态。
- 增加共享语言目录排序：推荐顺序会将目标应用当前实际语言置顶，并按当前界面语言中的名称进行本地化排序；语言组和语言变体分别提供五种排序方式及升降序切换。
- 为使用“系统默认”的应用增加设备实际 Locale 展示。
- 增加 Release、Release Unsigned、Debug 和 Debug Unsigned 四种独立构建身份，并通过已忽略的 `signing.properties` 支持私钥签名。

### 优化

- 将产品从 Language Selector 更名为 Fuyao Locale，将发布应用 ID 从 `vegabobo.languageselector` 调整为 `ing.fuyaoskyrocket.applocale`，版本更新为 27.0，Build 为 1A569。
- 将技术栈更新为 Kotlin 2.4.10、AGP 9.2.1、Compose BOM 2026.06.01、Material 3、Material 3 Adaptive、Hilt、KSP、Kotlin Flow 和 Java 21。
- 将原界面替换为基于 Token 的 Material 3 设计系统，支持动态明暗主题、全面屏手势 Edge-to-Edge、可复用列表组件及紧凑/宽屏自适应布局。
- 将应用详情、语言分组、搜索结果和语言变体统一到可复用语言选择器及单一带 key 的惰性列表数据流，并共享排序状态和本地化比较逻辑。
- 将顶部应用栏收敛为纯文本页面标题，同时保留返回和上下文操作的语义位置。
- 将应用包信息、Locale、本地存储、特权服务、页面状态和 UI 职责拆分为聚焦的模型、数据源、Repository、用例、ViewModel 和可复用 Composable。
- 将语言目录改为在列表外构建和缓存分组数据，并基于 Android 可用 Locale 衍生常用的语言、书写系统、地区和书写系统—地区别名。
- 将应用加载改为先展示包列表，再通过单次 Binder 批量读取 Locale；Activity 配置变化时复用完整快照，语言操作后只更新受影响项目。
- 将原关于页面替换为项目级双语内容、应用界面语言设置、Fuyao Locale 仓库链接、VegaBobo 原项目引用，以及适用的 AGPL-3.0-only 与 Apache-2.0 许可说明。
- Fuyao Locale 整体改用 AGPL-3.0-only，同时为源自 Language Selector 的部分保留上游 Apache-2.0 许可证与署名。
- 更新 Fuyao Locale 的应用图标、自适应图标、单色图标和快捷设置磁贴品牌资源。
