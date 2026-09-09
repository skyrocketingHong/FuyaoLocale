# Gingerbread 来源与适配

## 固定来源

- AOSP frameworks/base，android-2.3.7_r1，提交 3f2821425f1ab6eddb76a8725e3f2c3edb5d8b07。
- 控件来源 core/res/res；字体来源 data/fonts 的 DroidSans.ttf 与 DroidSans-Bold.ttf。
- 资源归档 SHA-256：627da3793259dc352bd418ae20d92e106beda242661938471cdbfb3cd119dd61。
- 导入入口 tools/import_android_theme.py，配置与逐项 SHA-256 在同目录 import-spec.json / asset-manifest.json；许可见 FONT_NOTICE.txt 和根 NOTICE。

[固定源码树](https://android.googlesource.com/platform/frameworks/base/+/3f2821425f1ab6eddb76a8725e3f2c3edb5d8b07/core/res/res/)、[Android 2.3 官方介绍](https://developer.android.com/about/versions/android-2.3-highlights)。

## 资源与组件

独立导入 319 个运行文件和 20 个参考文件，资源依赖闭包覆盖 141 个资源键。两个字体文件与 Eclair 字节不同，因此使用独立字体；不能由 Eclair 改名或简单染绿替代。按钮、Checkbox、Radio、输入、标题、菜单、标签、列表选择器、分隔、对话框、Toast 与进度均通过 ClassicResources 的版本映射读取 2.3 文件。

Gingerbread 与 Eclair/Froyo 共用经典控件的绘制和窗口机制，具体资源、字体与窗口不透明填充独立解析。2.3 黑色对话框正文与灰色按钮面对应导入的 9-patch；菜单面与文字角色采用该版本的暗色关系。原始位图、9-patch 和证据文件不改像素。

## 现代适配

- 经典 Android 的默认变体为 Gingerbread，旧 Eclair/Froyo 选择保持原值。
- 公共 Switch 尚未出现；布尔偏好用复选框，并保持整行可操作语义。
- 延续已接受的经典橙黄活跃标签。触控目标至少 48dp；系统字体兼容默认开启，以支持中文和 OEM 字体，关闭时使用年代原字体。
- 统一明暗模式、现代返回、焦点、权限与正文分页仍由应用共同机制承担。安装环境仍为 Android 13 及以上，参考版本不等于运行版本。
- 中文字形、9-patch 最终边界、按压反馈、窗口动效与真实触控由用户手动验收，当前不声明历史像素等同。
