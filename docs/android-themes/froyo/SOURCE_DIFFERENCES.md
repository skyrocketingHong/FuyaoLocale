# Android 2.2 Froyo 与 2.0 Eclair

Froyo 固定为 AOSP `android-2.2_r1`，提交 `71beeab14a891297b6658c4aeb47557083091aa1`。框架资源归档、SHA-256 和导入闭包见 `import-spec.json`、`asset-manifest.json`。

在本应用使用的215项框架资源中，201项字节相同。其余14项中，7份XML仅有BOM或格式差异，1份下拉按钮selector增加禁用、禁用且焦点、非焦点窗口下的禁用分支；6张横向不确定进度图片的像素发生变化，mdpi帧宽由33px变为36px。两份Droid Sans字体字节相同。逐项比较见 `eclair-comparison.json`。

因此Froyo独立保留其下拉按钮状态与进度资源，其他已确认一致的布局、按钮、复选框、单选框、字体与窗口机制共用经典后端。它不是一次全新的视觉体系。下拉选择器现已使用各版本的原始下拉背景，而非普通按钮背景。

新增18个运行资源及10份原始参考，全部通过既有 `tools/import_android_theme.py --era froyo` 流程导入，原位图不改写。布局与字体继续引用经上述比对确认的Eclair资源；两者激活标签都使用经典TabHost焦点样式的橙黄色反馈。

本次仅完成源码、资源和构建验证；真实设备与历史系统视觉比较尚未进行。

原始资源：[Froyo框架资源](https://android.googlesource.com/platform/frameworks/base/+/71beeab14a891297b6658c4aeb47557083091aa1/core/res/res/)。
