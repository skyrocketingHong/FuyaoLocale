# Android 主题实现与验收记录

当前源码为八个主题家族、11个变体，新增Gingerbread与原生Material 2的Material Rounded。五个主页面仍采用固定栏位的正文分页。下列记录按轮次保留，最新结果见文末“第十一轮设计语言家族”；契约见[设计系统](DESIGN_SYSTEM.md)。

## 第十轮历史验证记录

| 项目 | 结果 |
|---|---|
| 主题专项JVM测试 | PASS：34项，0失败／0错误；涵盖资源ID、主题目录、外观事务、窗口取消、队列释放、主从布局和Lollipop动画 |
| 离线资源导入测试 | PASS：6项；包括路径边界、DTD拒绝、public声明、私有属性、旋转转换及共享selector状态ID |
| 完整JVM测试 | 1B114曾69项通过；1B115复验为68/69通过，既有配置保存并发用例再次失败，详见下文 |
| 四种构建类型 | 最终结果与当前版本见下方产物记录 |
| 资源完整性 | PASS：Eclair 218＋17、Honeycomb 347＋27、KitKat 683＋93、Lollipop 588＋165（运行记录＋原始参考）；全部文件SHA-256匹配各自清单 |
| 修改边界 | PASS：相对round-10初始源码快照，非UI生产业务文件及ViewModel均未变化 |
| 设备与历史原版对照 | UNVERIFIED：未连接设备／未运行历史Android；不宣称逐像素、真实系统语言写入或OEM行为验证通过 |

源码范围、资源核对和产物详情保存于plans/round-10的scope-validation.json、resource-validation.json、delivery-build-result.json；plans保留为本地执行材料，不用于应用运行。

## 独立覆盖矩阵

- [Android 2.0 Eclair](eclair/ACCEPTANCE.md)：经典标题栏、TabWidget、立体按钮、Checkbox设置、可见菜单与底部多选面板。
- [Android 3.0 Honeycomb](honeycomb/ACCEPTANCE.md)：API11原始Holo资源、独立字体／度量／窗口、平板主从和共用标签；手机布局属于现代适配。
- [Android 4.4 KitKat](kitkat/ACCEPTANCE.md)：固定4.4资源、Roboto和背景，同族控件共享机制，资源映射独立。
- [Android 5.0 Lollipop](lollipop/ACCEPTANCE.md)：初代Material颜色、逐帧控件、Ripple、Toolbar、顶部标签、淡入淡出弹窗。

每个矩阵独立填写C01—C17、O01—O07、P01—P10，共136项归属记录；源码／构建与设备／原版视觉分开判定。没有把旧主题或历史轮次的设备结果继承为新主题结果。

## 既有并发测试不稳定项

`ConfigurationEditCoordinatorTest.saveFailureKeepsPendingAndRetrySavesOnlyWithoutReapplying`在完整测试中曾出现保存次数超额：首次Eclair阶段出现预期1、实际2；1B115复验出现预期2、实际3。构造时恢复pending与随后提交使用并发调度，可能重复进入保存。`ConfigurationEditCoordinator`及对应测试与本轮初始快照一致，本轮未修改业务策略或放宽断言。完整测试不能因此记为稳定全绿；主题专项测试与安装包构建结果单独记录。

## 后续设备验证

调试构建新增Theme Gallery，不修改生产外观偏好或设备语言，用于七种主题的明暗、文字、启用／禁用、按下／焦点、选择、输入、Popup、对话框、长目录和反馈检查。另有原Holo与KitKat调试入口。

待运行100%／200%字体、RTL、窄屏／横屏／宽屏、主从窗口缩放、IME、按下—拖出—取消、开关拖动／取消、键盘激活和低动效检查。真实单应用／批量语言、系统语言草稿和配置操作需在主应用验证。历史界面比较须使用同内容、同逻辑尺寸与密度的原版参考；系统栏、中文字体fallback和Compose宿主差异单列。

## 第十轮首次完整产物（历史记录）

1B117／营销版本27.1／versionCode 271117。四种类型均BUILD SUCCESSFUL，共20个APK；10个签名包通过apksigner，10个Unsigned包确认未签名。每种类型均含universal、arm64-v8a、armeabi-v7a、x86、x86_64；全部原生ELF架构匹配，同类型五包的DEX内容相同。当前Release universal为9,106,721字节。

文件名保留`x86_64`下划线。当前项目没有自有NDK源码交叉编译；依赖自带的`libandroidx.graphics.path.so`按ABI分包。分包的主要DEX与资源相同，因此总大小相近，不能据此判断架构无效。

最终构建命令：`./gradlew :app:assembleRelease :app:assembleReleaseUnsigned :app:assembleDebug :app:assembleDebugUnsigned --no-daemon`。主题专项测试在1B116通过；随后仅补齐Eclair确定进度与调试示例，1B117再次完成四种打包。最终文件和校验值从各变体output-metadata.json读取，不按旧编号猜测。


## 用户反馈修订（27.2 / 1C86）

2026-09-08，根据七张截图和后续要求完成八项代码调整：系统字体与字重兼容、复古窗口不透明内容及原生边框内距、单行顶部标签、Eclair标题栏菜单、五主页面横滑、持久化应用设置、移除重复关于标题，以及四语言关于文案更新。此前“ViewModel均未变化”仅指第十轮初始验收；本次MainViewModel已接入系统应用筛选偏好的读取和保存，语言写入与配置协调逻辑未修改。

新增八项设置分别控制：复古主题系统字体、系统应用、包名、应用类型、地区旗帜、横滑切页、双击标签回顶、记住上次页面。关闭双击只影响触摸手势，无障碍与键盘回顶入口保留。关于页项目链接、一个中国原则声明和许可归属保持。

- 专项JVM测试：51项通过，0失败、0错误、0跳过，覆盖主题资源/生命周期、偏好持久化、切页方向/首尾/RTL/启动恢复规则和双击检测。
- Release：BUILD SUCCESSFUL，营销版本27.2，Build Number 1C86，versionCode 272086；universal及四个ABI共5包均通过签名验证。其他构建类型未在本次重打包。
- 四语言XML：解析与重复名称检查通过；git diff --check通过。1C85完成51项专项测试；其后整理关于页缩进与四语言地区显示声明，1C86重新完成Release构建，功能逻辑未再变化。
- 本次没有运行完整JVM套件、设备或视觉检查；上方记录的既有配置并发测试不稳定项不据此视为已修复。用户明确自行进行视觉检查，系统/OEM字体、真实触摸与窗口显示尚未验收。

专项测试与首次构建命令：`./gradlew :app:testDebugUnitTest --tests '*TopLevelNavigationRulesTest' --tests '*UserPreferencesStoreTest' --tests '*TabDoubleTapDetectorTest' --tests 'ing.fuyaoskyrocket.applocale.ui.designsystem.*' :app:assembleRelease --no-daemon`。

最后文案更新后运行`./gradlew :app:assembleRelease --no-daemon`，得到1C86产物并重新核对签名与四语言资源。


## 九主题与正文分页修订（27.2 / 1C90）

- KitKat：白色选中指示与灰阶强调；明亮正文搭配原暗色实体栏。原4.4框架的蓝色资源保留，通过限定资源集合的绘制处理呈现用户指定的白灰方向，不把框架原本仍有的蓝色说成不存在。
- Eclair/Froyo：活跃标签使用原橙黄焦点资源；单击即时导航，双击观察不消费事件，并保留无障碍回顶。Froyo独立导入18项运行资源，主要差异为下拉禁用/焦点状态和进度动画；两版Droid Sans字体相同，详见[Froyo比较](froyo/SOURCE_DIFFERENCES.md)。
- Material系列：初代Material Design、Material You和Expressive分别对应不同主题与组件策略，详见[对应与官方来源](MATERIAL_GENERATIONS.md)。Expressive组件使用官方1.5.0-alpha27，底层Compose解析至1.12.0-beta01。
- 正文分页：HorizontalPager与固定栏位分离，已删除旧整页NavHost横移动画；保留唯一NavController用于主宿主和详情。页面模型键隔离，预览数据可更新，隐藏页不处理返回或一次性UI事件。宽屏标题与提示按原主从两栏显示。
- 草稿：系统语言初次读取幂等，已有数据和草稿不被重进页面重载覆盖；读取完成时也保留等待期间出现的编辑。

验证结果：

1. 49项专项JVM测试全部通过，0失败、0错误、0跳过；覆盖原主题资源/窗口契约、九主题目录、明暗模式、偏好、双击、启动页，以及分页模型键和生命周期边界。此次测试在1C89执行，之后仅补充系统语言初次读取完成时的草稿保护，再完成最终Release构建。
2. 最终Release构建成功：1C90 / versionCode 272090，五个APK均通过签名核对。未重打包其余三种构建类型。
3. Eclair 218、Froyo 18、Honeycomb 347、KitKat 683、Lollipop 588项已导入运行文件均符合各自清单SHA-256；四语言XML、主题说明及修改格式检查通过。
4. 未执行设备、模拟器或视觉检查，按用户要求由其自行检查；也未重新运行完整JVM套件，历史配置并发不稳定项不据此视为已修复。

本机新依赖下载遇到全局Aliyun镜像证书不匹配，单次构建通过临时-I脚本排除该镜像并使用Google官方源，未关闭TLS校验、未修改全局镜像配置。专项测试命令为`./gradlew -I /tmp/fuyao-official-repositories.gradle :app:testDebugUnitTest --tests '*PageScopingTest' --tests '*TopLevelNavigationRulesTest' --tests '*UserPreferencesStoreTest' --tests '*TabDoubleTapDetectorTest' --tests 'ing.fuyaoskyrocket.applocale.ui.designsystem.*' :app:assembleRelease --no-daemon`；最终草稿保护后仅重新执行`:app:assembleRelease`。


## 第十一轮设计语言家族（27.2 / 1C98）

2026-09-09，按获批 A—E 方案完成分类兼容、八类主题规格、共用语义组件、Gingerbread、Material Rounded及全家族收敛。

- 八个一级家族、11个变体；九个旧存储值保持，经典Android默认Gingerbread，Holo保留ICS蓝色/KitKat灰白。离开和进入家族时均保存有效变体，明暗/效果独立。
- 功能页面的列表、筛选、工具栏、多选、关于分节、拖动外观和主从布局交给设计系统。架构边界检查通过；本轮开始时已有的6个页面模型逐字节保持，权限与语言业务机制沿用原实现。
- Gingerbread固定2.3.7_r1，319个运行文件、20个参考文件、独立字体。Material Rounded使用原生Material 2 1.11.4。Material更新应用配色与纸面层级，原始AOSP文件保持。
- 最终55项针对性JVM测试全部通过，0失败/错误/跳过；包含6项新家族/兼容测试及既有主题、模式、窗口、偏好、双击、导航和页面模型边界测试。离线资源导入器6项测试通过。
- 七份主题清单中的2655个运行文件及332个参考文件SHA-256核对通过，四语言新增项、XML结构与重复名称核对通过。
- 最终Release构建成功，1C98 / versionCode 272098。universal及四个ABI共5包，签名、包名/版本/最低API、ZIP完整性、各ABI原生ELF及同组DEX一致性逐包核对，结果保存于本地plans/round-11/delivery-verification.json。
- 本轮没有重新运行完整JVM套件；前述配置保存并发不稳定项不因此视为已修复。没有构建设备测试、运行模拟器、安装到设备或进行视觉检查。其他三种构建类型未重打包；Debug主题图库已参与编译。

初次专项检查中，旧Lollipop系统栏断言仍预期灰色，已依据本轮固定靛蓝主色更新为#303F9F并保留亮色图标检查。最终结果来自1C98，未沿用中途失败编号。单次构建通过-I排除本机证书不匹配的Aliyun镜像，使用官方源；未关闭TLS或改写全局镜像配置。

手动验收由用户完成，见[清单](MANUAL_CHECKS.md)。上述PASS仅适用于源码、资源、测试和打包，不包含历史像素、字体、OEM、真实权限或语言写入验收。

## 六项反馈修订（27.2 / 1C100）

- 主题/变体按参考Android版本排序；默认值与历史选择不变。四语言全部19个目录项的名称和说明包含版本，使用Ice Cream Sandwich等完整名称；miuix的13+明确为应用运行要求。
- 玻璃/普通底栏切换保留旧形态退场，完全隐藏后更换并滑入，快速反向请求保持最新目标。
- 交互设置行恢复真实开关回调；Holo统一测量与填充边界，补充连续拖动和释放/取消动画。Material 3按该版本官方尺寸、颜色和MotionScheme补充拖动；M2、miuix、Lollipop继续使用各自后端。
- 关于文案精简，保留必要条件、地区声明、项目引用与许可；同一主题各页的内容边界与分类/正文统一到主题规格，修正重复边距。
- 60项针对性JVM测试通过；Release 1C100 / 272100构建成功，5包签名、版本、原生ABI、ZIP与同组DEX核对通过。四语言版本文案、源码边界与修改格式检查通过，6个页面模型保持。

实际触摸、动画和视觉对齐尚未自动验收，按用户要求手动完成，见[六项反馈复查](MANUAL_CHECKS.md)。本轮未重跑完整JVM套件，既有配置保存并发不稳定项保持原记录。详细执行及产物证据位于本地plans/round-11/FEEDBACK_EXECUTION.md和feedback-delivery.json。
