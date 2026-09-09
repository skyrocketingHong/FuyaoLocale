# Android 3.0 Honeycomb：版本差异与适配

来源为官方 Android SDK Platform 3.0 revision 2、API 11、构建 HONEYCOMB 104254。归档、资源许可及校验见 `SOURCE.md`、`RESOURCE_NOTICE.txt` 和 `asset-manifest.json`。共用 Holo 控件按 `HoloAsset` 语义名称请求资源，`HoneycombAssets.kt` 直接映射到本版本资源；不通过 ICS 的资源 ID 或浅色映射回退。

## 与 ICS 分开实现的内容

- Droid Sans Regular/Bold 直接来自该 SDK 的 `data/fonts`。哈希与单独取得的 Android 2.0 字体相同；运行资源仍使用本主题前缀，未引用 Roboto。
- `Theme.Holo` / `Theme.Holo.Light` 分别提供原始文字状态表和硬件加速窗口背景 PNG。Action Bar 背景在本版为 `@null`，因此保持透明，显示原版窗口纹理；不套用 ICS 的实色背景。
- Action Bar 为 56dp，ActionButton 最小宽度 64dp、左右内边距 16dp。顶部标签使用 `tab_indicator_holo`、18sp 常规文字，未沿用 ICS 的 12sp 粗体标签。
- 两行列表沿用 64dp 最小高度、22sp 标题和 14sp 摘要，内容起始位置按 `simple_list_item_2.xml` 的 6dp；设置行按 `preference.xml` 使用 15dp 起始边距。分类标题按原始样式保留大小写、14sp 粗体和 5sp 起始内边距。
- 设置使用公开时代的 CheckBoxPreference。SDK 虽包含内部 Switch 资源，公开 `android.jar` 没有 `android.widget.Switch`，因此未将这些内部资源作为 3.0 设置开关入口。
- 圆形进度的旋转周期为 3500ms；输入文字、菜单和对话框按钮为 18sp。CAB 使用本版背景和有状态关闭资源，关闭动作提供本地化无障碍名称。
- AlertDialog 使用本版 60dp 标题区、32dp 内容边距、4dp 原始强分隔线、54dp 按钮区；确认在左、取消在右。目录窗保持本版 Light/Dark，单选文字按原布局取 18sp、左右 16dp。进入/退出的 220ms 缩放、150ms 透明度及减速曲线与取得的原版 XML 一致。

## 平板与现代窗口适配

应用和配置页面复用既有主从业务状态。应用左右两栏共用一套顶部标签，按两栏实际标题高度对齐内容；不绘制两套标签，不添加现代 Rail 或玻璃 Dock。窗口背景跨两栏统一绘制。选择应用的状态提升到自适应导航外层，窗口变窄时继续显示该应用详情；列表滚动状态与双击标签回顶入口仍由原页面接收。

窄屏、现代返回手势、48dp 最小操作目标、长文本增高和现有业务图标属于本应用适配。系统状态栏和导航栏由真实系统提供，不绘制 Honeycomb 的时钟、电池或系统按钮。消息队列显示的是应用内反馈，不能等同于运行在旧系统上的原生 Toast。

## 验证

资源清单保留原图、原始 XML、字体、来源和哈希。已进行编译、资源隔离、窗口与布局几何测试及 Release 构建；具体构建编号和结果记录在第十轮执行日志。真实设备重排、触控、TalkBack、截图及与 Android 3.0 原版逐项对照尚未运行。现代 Android Drawable 实现的运行时表现也不以源码和哈希检查代替视觉对照。
