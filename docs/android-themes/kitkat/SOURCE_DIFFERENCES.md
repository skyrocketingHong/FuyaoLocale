# Android 4.4 KitKat 与 4.0 ICS 的复用边界

资源固定在frameworks/base android-4.4_r1（1aed5ea2dcf7cb0bf35635f378b2326461ba0df0），原始位图、九宫格、Roboto字体均按原字节导入；命名和XML引用增加kitkat_前缀。清单为asset-manifest.json，重建输入为import-spec.json。

| 范围 | 4.4证据与实现 | 与ICS的边界 |
|---|---|---|
| 按钮、CheckBox、Radio与Switch | 原4.4 selector、各密度位图、状态顺序；64×48dp按钮、96dp Switch | 沿用Holo同族布局与状态机，各绘制站点仅解析4.4资源 |
| 列表 | Theme.Holo：48/64/80dp，8dp边距，对话框16dp | 同一指标，保留业务原有行状态，不复制业务页 |
| Action Bar | 使用原Dark.Solid资源，明亮正文搭配DarkActionBar；48/40land/56sw600dp | 4.4独立资源，固定栏与分页正文分离 |
| 交互色 | 本应用按用户选定的4.4视觉方向，改为白色选中指示与灰阶高亮；明亮正文使用可读的深灰强调 | KitKatControlPalette只处理4.4的强调控件，不影响ICS和Honeycomb |
| 窗口背景 | Dark黑到#272d33；Light #e8e8e8到白，原gradient XML | 普通平面容器保持透明，让窗口渐变连续；ICS原背景保留 |
| 字体 | 原4.4 Roboto Regular/Bold，两文件有独立hash | 不使用4.0字体文件替代 |
| Dialog/Popup/进度 | 4.4原资产与原动画结构；关闭完成复用既有单次回调事务 | 窗口状态机共用，绘制资源独立 |

共享渲染器以已有Holo资源ID作为兼容键，KitKatResources对每个键显式返回kitkat_资源；不调用ICS Light/Dark解析器，未知键直接报错。每个Drawable站点由legacy层独立mutate，资源/字体切换随CompositionLocal变化。

原始XML参考覆盖themes、styles、colors、dimens、相关布局及动画。资源字节相同或不同不等同于视觉相同或不同，未把文件哈希比较写成像素对照结论。

现代适配：保留48dp命中区域、TalkBack/键盘、RTL、字体缩放、现代返回取消；五个顶部Tab在窄屏可横向滚动。系统栏使用不透明黑色安全区，不复刻系统时间、电量、导航键，不强制壁纸透明。

验证：独立debug控件页构建1B97通过；完整页面接入1B98通过；资源隔离回归与最终Release 1B99通过。当前无连接设备/模拟器，真机与历史像素对照均UNVERIFIED。


## 白色强调色修订

固定4.4框架中的Holo控件确实仍包含蓝色资源；同版本Dialer源码也同时使用白、灰和蓝色，不能据此宣称4.4所有原生控件均为白色。本应用按用户明确要求采用KitKat白色指示与灰阶强调，通过`KitKatControlPalette`在绘制时调整指定控件，保留原始资源文件、透明通道、边缘与状态几何。暗色栏使用白色强调，浅色正文使用灰色以保持可辨认性；错误语义色不参与处理。

标题和标签使用原始暗色实体Action Bar资源，避免明亮正文透过暗色透明资源后造成白字不可读。Eclair、Honeycomb、ICS与Material Design的资源和配色不参与该处理。

[AOSP 4.4主题定义](https://android.googlesource.com/platform/frameworks/base/+/1aed5ea2dcf7cb0bf35635f378b2326461ba0df0/core/res/res/values/themes.xml)，[AOSP 4.4 Dialer颜色](https://android.googlesource.com/platform/packages/apps/Dialer/+/android-4.4_r1/res/values/colors.xml)。
