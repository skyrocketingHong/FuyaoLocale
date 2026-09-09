# Material 系列对应与实现

| 主题家族 | 设计参考 | 本应用实现 |
|---|---|---|
| Material Design（Android 5.0–8.1） | 2014 年初代 Material，Android 5–8 时期 | 固定 Android 5.0 资源、靛蓝主色与深色系统栏、纸面阴影、初代涟漪、Toolbar 与顶部标签；稳定标识 MATERIAL_LOLLIPOP |
| Material Design 2 Rounded（Android 9–11） | Android 9–11 时期圆润的 Material 2 应用语言 | 原生 androidx.compose.material 1.11.4，固定蓝色强调、独立中性明暗表面、圆润搜索、胶囊按钮、BottomNavigation 与 NavigationRail；稳定标识 MATERIAL_ROUNDED |
| Material You / Material Design 3（Android 12–15） | 常规 Material 3，Android 12–15 为代表 | 壁纸动态配色、色调表面、常规 MaterialTheme 与 standard MotionScheme；稳定标识 MATERIAL_YOU |
| Material Design 3 Expressive（Android 16+） | Material 3 Expressive，Android 16 及以后为代表 | MaterialExpressiveTheme、弹性 MotionScheme、强调排版、形变按钮、LoadingIndicator 与 ShortNavigationBar；稳定标识 MATERIAL3_EXPRESSIVE |

版本范围表示参考时期，不代表这一时期所有应用具有统一外观。Material Rounded 是本项目的名称，其具体圆角与配色属于应用设计配置。miuix 继续作为单独的扩展家族安装原生主题。

Material 2 版本由现有 BOM 2026.06.01 解析为 1.11.4；Material 3 独立固定 1.5.0-alpha27，底层 Compose 解析至 1.12.0-beta01。本轮没有整体升级 BOM。Material 2 与 Material 3 分别安装自己的原生主题，共用中立 App 组件契约，M2 路径不借用 M3 默认控件。

Material 的应用自有配色在 material_classic_profile.xml 中声明：主色 #3F51B5、深色 #303F9F，控件强调继续使用固定 5.0 资源。Toolbar/标签使用白色内容；Popup 和对话框清除栏内颜色上下文。原始导入资源不改字节。卡片/分组、栏位、菜单、对话框分别消费 2/4/8/24dp 层级配置；普通记录与关于页分节仍保持连续。

所有家族共用一个 NavController 和固定栏位的 HorizontalPager 正文分页。每页模型键、预览状态、返回和一次性事件的生命周期边界保持独立。

依据：[Material 2 与 Material 3 对应](https://developer.android.com/develop/ui/compose/designsystems/material2-material3)、[Material 形状系统](https://developers.googleblog.com/en/building-the-shape-system-for-material-design/)、[2014 年 Material Design](https://developers.googleblog.com/en/this-is-material-design/)、[MaterialExpressiveTheme](https://developer.android.com/reference/kotlin/androidx/compose/material3/MaterialExpressiveTheme.composable)。

实现边界见 [设计系统契约](DESIGN_SYSTEM.md)，验证状态见 [验收记录](ACCEPTANCE.md)。编译与资源核对不替代真机或视觉验收。
