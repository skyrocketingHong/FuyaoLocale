# Android 5.0 Lollipop：来源与适配边界

基准为 AOSP frameworks/base `android-5.0.0_r1`，固定提交 `ba35a77c7c4494c9eb74e87d8eaa9a7205c426d2`。原始资源包、单文件 SHA-256、字体和转换规则分别见 import-spec.json、asset-manifest.json、FONT_NOTICE.txt。Ripple、RippleBackground、RippleDrawable、Switch 和 ViewConfiguration 的原始源码与哈希见 framework-sources.json。

## 1. 固定资源与主题继承

| 角色 | Light | Dark | 原始依据 |
|---|---|---|---|
| 页面背景 | #EEEEEE | #303030 | background_material_* |
| 顶栏主色 | #BDBDBD | #212121 | primary_material_* |
| 系统栏深色 | #757575 | #000000 | primary_dark_material_* |
| 强调色 | material_deep_teal_500 | material_deep_teal_200 | accent_material_* |
| 浮动对话框背景 | #EEEEEE | #424242 | Theme.Material[.Light].BaseDialog |
| Ripple资源色 | #40000000 | #40FFFFFF | ripple_material_* |

框架纹理与逐帧图片保持原始字节；XML只作资源前缀、属性命名空间和状态ID适配。字体是同一提交的 Roboto Regular、Medium、Bold，不复用ICS或KitKat字体。中文等缺字使用运行系统fallback；其字形不能视为2014原版字形。

## 2. 控件与动作

- 普通按钮按Widget.Material.Button取48dp最小高度、88dp最小宽度、4dp/6dp背景内缩、2dp圆角；背景静止1dp、按下增加2dp，100ms过渡，抬起延迟100ms。文字动作使用原式14sp中等字重与大写规则。
- 复选框、单选框保留原始32dp图像和16帧、每帧15ms的状态动画；开关保留12帧图像，平移采用Switch.java的250ms。开关几何从原图padding和opticalInsets计算，支持点击、拖动、RTL和取消；取消不提交。手势识别使用Compose，尚未与API21设备动态对照。
- `colorSwitchThumbNormal`是私有框架属性，运行资源改为应用内`lollipop_colorSwitchThumbNormal`，仍绑定原版Light/Dark selector。原开关的disabled-off与normal-off共用off状态ID；仅该animated-selector局部忽略不适用的布局DuplicateIds检查，原文和转换均留证。
- 进度使用原版vector、animator、pathInterpolator和水平层列表。运行系统负责这些公开Drawable类型的解析和调度；原版动态一致性仍未验证。
- EditText使用原始未激活／激活下划线图片、18sp文字和强调色光标。Compose管理文本、选区、IME和无障碍；不依赖现代Material文本框默认值。

## 3. Ripple移植

LollipopRipple从固定Ripple.java与RippleBackground.java移植进入延迟80ms、半径／密度加速度方程、333ms透明度退出、原对数退出曲线及背景透明度阶段；热点按pointer位置更新并限制到目标圆范围，绘制层按原式使用资源alpha的一半。指针观察不消费业务手势，取消不会提交操作。每次按下独立保留一个波纹，最多10个，节点移除会取消任务。

不调用现代RippleDrawable。绘制后端使用Compose Canvas，动画运行于Compose生命周期；原版RenderThread实现、GPU混合及逐帧截图仍未对照。低动效由Compose动画时钟缩放处理，设备实际设置切换仍待验证。

## 4. 页面、窗口与适配

- 顶栏高度从原版限定符资源解析（标准56dp），顶部标签按该提交Widget.Material.ActionBar.TabText的12sp粗体规则绘制；五个目的地共用现有导航与回顶状态。宽屏主从共用一套标签，窄屏保留选中详情。
- Toolbar原始标题尺寸20dp在本应用按20sp处理，以遵循现代字体放大设置；标题和长标签允许换行。导航图标触控目标至少48dp；这是明确的无障碍适配。
- 对话框采用2dp圆角、24dp正文横向留白、18dp顶部留白、16dp浮动高度；按钮顺序为取消在左、确认在右，字体放大时允许换行。Animation.Material.Dialog实际引用popup_enter/exit_material，只有150ms三次减速alpha动画，不叠加Holo缩放。
- Popup使用原版应用背景角色、2dp圆角和16dp高度，48dp菜单行；Compose Popup提供现代焦点与屏边约束。
- 反馈使用该提交toast_frame和文字样式，在应用内排队展示；页面上的重试、错误与未完成操作入口继续保留。
- Search、Back、Overflow、Refresh、SelectAll使用固定提交的Material框架图像；设置、增删、信息、排序、前进等业务命令使用同一提交保留的框架图像，统一着色保证明暗可读。收藏用原Material星形表示，不是历史系统的pin控件。具体映射在LollipopIcons.kt；这部分不声称是所有2014应用共同采用的图标集合。
- 所有年代的滚动条原主题参数均为停止后300ms等待、250ms淡出，使用同一生命周期机制。共享Drawable绘制器通过外部合成处理调用方alpha，不调用setAlpha(255)覆盖原始状态器子项透明度。

## 5. 验证口径

源码、资源链接、专项测试和构建结果见本目录ACCEPTANCE.md及上级ACCEPTANCE.md。真机、历史系统、字体放大、RTL、输入法、窗口拖动和动态像素对照均为UNVERIFIED；调试构建提供Theme Gallery供后续逐项检查，不把它的存在写成已完成视觉验证。


## 第十一轮应用主题配置

本节替代上文默认灰色主色与浮动层高度的应用配置说明。固定 5.0 资源文件保持不变；新增应用自有 material_classic_profile.xml，选择 #3F51B5 主色与 #303F9F 深色。Toolbar/顶部标签使用白色内容，搜索和命名图标读取栏内前景；菜单和对话框独立恢复正文前景。Material 纸面层级由 ThemeSpec 声明，卡片/分组 2dp、栏位 4dp、菜单 8dp、对话框 24dp。普通应用、语言与配置行以及关于页分节仍无逐行卡片壳。以上是本应用的初代 Material 设计配置，不宣称每个 2014 系统应用均采用这些参数。
