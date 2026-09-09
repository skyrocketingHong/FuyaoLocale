# 主题家族与组件契约

Fuyao Locale 提供七个 Android 设计语言家族和一个 miuix 扩展家族，共八个一级选项、11 个稳定变体。明暗模式独立于主题。家族决定设计语言，变体承载版本资源差异，后端负责实际组件呈现。

## 1. 目录与存储

| 家族 | 默认变体 | 兼容变体 | 后端 |
|---|---|---|---|
| 经典 Android | GINGERBREAD | ECLAIR、FROYO | Classic，内部 AppControlFamily.Eclair |
| Honeycomb | HOLO_HONEYCOMB | 无 | Holo 共用机制，独立 3.0 SDK 资源 |
| Holo | HOLO_ICS | HOLO_KITKAT | Holo 共用机制，版本资源独立 |
| Material | MATERIAL_LOLLIPOP | 无 | Lollipop，固定 5.0 资源与应用配色 |
| Material Rounded | MATERIAL_ROUNDED | 无 | 原生 Material 2 |
| Material You | MATERIAL_YOU | 无 | 常规 Material 3 |
| Expressive | MATERIAL3_EXPRESSIVE | 无 | 原生 Material 3 Expressive |
| miuix | MIUIX | 无 | 原生 miuix |

ThemeVariant 保留为 AppThemeStyle 的类型别名，ThemeBackend 为 AppControlFamily 的别名；原枚举类与九个存储字符串不改名。app_theme/style 是当前变体的唯一持久化身份，家族由目录映射得到。未知标识回退到 Material You，初始化不回写原值。

last_variant.FAMILY 记录家族上次使用的变体。切换时在同一次编辑中保存离开与进入家族的变体，保证没有新记录的旧安装也能返回原 Froyo/KitKat。显示顺序独立于选择：家族按参考 Android 版本排序，变体按版本升序，扩展风格单列。当前家族优先保留当前变体；其他家族优先恢复有效历史值，首次使用才采用默认变体。跨家族错误记录在读取时丢弃。

设置分为“早期 Android／Material 体系／扩展风格”。只有经典 Android 和 Holo 显示二级细节入口。选择窗口关闭完成后才提交现有外观事务；失败收敛到实际已保存状态，保留两项独立效果的最新值。

## 2. 八类主题规格

AppThemeProvider 是唯一生产分发入口，各原生主题通过 AppUiThemeValues 生成一份 ThemeSpec。页面继续通过 AppUiTheme 读取中立角色。

| 规格 | 数据与消费者 |
|---|---|
| Color | AppPalette；文字、选中状态、表面、警告及配对前景 |
| Typography | AppTextStyles；标题、正文、元信息和标签，保留系统字体兼容 |
| Shape | AppShapeTokens；行、分组、卡片、按钮、输入和对话框；胶囊按比例解析，miuix 保留 squircle，年代控件读取原框架资源 |
| Spacing | AppSpacingTokens 与 AppUiMetrics；内容边界、行外距、分组间距、图标槽、标题/标签高度、主从间距 |
| Elevation | AppElevationTokens；Material 的纸面层级与 M2 原生 Surface，现代主题按其原生表面规则呈现 |
| Motion | AppMotionTokens；选择状态反馈、批量栏入场与选择窗口呈现路径；实际年代窗口曲线来自固定版本，M2/M3/miuix 保留原生动效 |
| Icon | AppIconTokens；命名图标来源、动作/标签/身份图标尺寸；真实应用图标由功能层提供 |
| Component | AppComponentTokens；工具栏动作、筛选、多选与其余表现策略 |

规格只保存数据。Drawable、交互源、动画状态和回调由控件实例管理，不放入 ThemeSpec。图标或控件缺少历史资源时采用已有文字动作和菜单规则，不从其他年代借用资源冒充。

## 3. 十二类组件与页面接入

| 能力 | 统一入口／消费位置 |
|---|---|
| Button / IconButton | AppControls、AppToolbarIconButton；原生 M2/M3/miuix 与年代控件穷尽分发 |
| TextField / SearchField | AppSearchField、AppSearchableTopAppBar；M2 使用独立中性圆润输入面 |
| BooleanPreference / Switch | AppBooleanPreference；整行语义与 Checkbox/Switch 一起适配 |
| Checkbox / Radio / ChoiceRow | AppCheckbox、AppRadioButton、AppLocaleChoiceRow、AppChoicePreference |
| Surface / Card / Section | AppSurface、AppCard、AppPanel、AppSectionSurface；普通列表仍连续 |
| ListRow / ListItem | AppListRow、AppSettingsRow、AppReorderRow；功能层提供应用事实、Locale 状态及动作 |
| TopBar / ToolbarActions | AppTopAppBar、AppToolbarActions、AppSelectionScaffold；页面不判断后端 |
| NavigationBar / Rail | AppNavigationBar / AppNavigationRail 自行判断是否适用，唯一导航状态不复制 |
| Tabs | EclairTopTabs、HoloTopTabs、LollipopTopTabs；固定栏位和同一分页状态 |
| Dialog / Sheet / Menu | AppAlertDialog、AppModalBottomSheet、AppDropdownMenu；关闭完成再提交选择 |
| Snackbar / Feedback | AppSnackbarHostState / AppSnackbarHost；原生 M2/M3/miuix 或年代队列 |
| ProgressIndicator | AppCircularProgressIndicator、AppLinearProgressIndicator；原年代资源或原生进度/Expressive 组件 |

AppListDetailLayout 只接收列表与详情内容槽，负责对齐主从栏位和共享标签；页面模型与业务动作留在功能层。AppInfoSection 保留关于页连续分节；AppDragSurface 只负责拖动副本的外观，排序状态、坐标、尺寸、草稿仍由系统语言页持有。

应用、配置、语言行提供文字、图标和回调给通用行组件。排序同时提供循环切换动作与直接选择动作，设计系统决定显示为 chips 或菜单。刷新仍通过现有回调执行，传统窗口的工具栏提供可见入口，支持下拉的后端保留原生刷新。

## 4. 状态与验证边界

- 一个 NavController；HorizontalPager 仅移动正文，固定栏位保留。
- page/模型类型/自定义 key 继续隔离页面模型；相邻页允许状态预览，返回与一次性事件只交给当前页。
- 不因切换主题新建业务模型、增加权限请求、重载应用列表或清空语言草稿。
- Material 2 弹层使用原生 ModalBottomSheetLayout，关闭过程保持窗口挂载；重开取消旧关闭，关闭完成回调只在隐藏后执行。
- 导入来源与校验记录保持可追溯；运行环境、原版像素、OEM 字体、触摸与窗口效果由手动验收确认。

执行 `python3 tools/verify_theme_boundaries.py` 可检查功能页面没有身份/后端呈现判断或原生控件依赖，设计系统没有业务页面模型。窗口尺寸信息属于导航布局输入，允许使用 Jetpack adaptive 查询；设置中的效果可用性说明也属于实际产品状态。

最新结果见 [验收记录](ACCEPTANCE.md)，手动项目见 [手动验收清单](MANUAL_CHECKS.md)。


## 5. 六项反馈后的控件约定

所有主题选项的名称与说明都包含 Android 版本及完整名称；Ice Cream Sandwich 不缩写成 ICS。miuix 不绑定一个历史 Android 设计版本，13+ 明确指本应用的运行要求。

玻璃/普通底栏之间的切换通过 BottomDockFormState 保存仍在退场的呈现类型，完全隐藏后才换后端并滑入。路由和多选抑制继续共用原 presence，页面内容树不复制，退出中的玻璃采样持续到动画结束。

设置行将真实回调交给滑块，并清除子控件的重复无障碍入口和焦点；整行仍可点按。M2 与 miuix 沿用原生拖动，Lollipop 沿用年代后端的拖动；Holo 与 M3 共用连续位置/释放提交/取消复位机制。Holo 按文字与 9-patch padding 测量，并用相同边界绘制滑块。当前 AndroidX Material 3 1.5.0-alpha27 的 Switch 源码仍标注待补 Swipeable，本应用使用其 52×32dp 轨道、状态颜色和 MotionScheme，补充连续拖动。

页边距以 AppUiTheme.spacing.contentInset 为准：经典 Android 15dp、Honeycomb 6dp、Holo 8dp、Material 系列与 miuix 16dp。普通行外距与内部边距之和等于内容边界；分类标题和关于正文使用同一来源。弹窗保留各自窗口的内距，不把背景 9-patch 与页面外距重复相加。
