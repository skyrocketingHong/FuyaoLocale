package ing.fuyaoskyrocket.applocale.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.components.AppNavigationDestination
import ing.fuyaoskyrocket.applocale.ui.designsystem.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.*
import ing.fuyaoskyrocket.applocale.ui.designsystem.lollipop.LollipopProgress
import ing.fuyaoskyrocket.applocale.ui.screen.*
import kotlinx.coroutines.launch

/** Debug-only acceptance surface. Choices are local; no app preferences or device languages change. */
class ThemeGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ThemeGallery() }
    }
}

@Composable
private fun ThemeGallery() {
    var style by remember { mutableStateOf(AppThemeStyle.ECLAIR) }
    var dark by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    var alert by remember { mutableStateOf(false) }
    var picker by remember { mutableStateOf(false) }
    var confirmations by remember { mutableIntStateOf(0) }
    var dismissals by remember { mutableIntStateOf(0) }
    var destination by remember { mutableStateOf(AppNavigationDestination.Home) }
    val scope = rememberCoroutineScope()
    AppTheme(darkTheme = dark, style = style) {
        val feedback = rememberAppSnackbarHostState()
        CompositionLocalProvider(LocalTopLevelNavigation provides TopLevelNavigationState(destination,
            topLevelDestinations(), { destination = it }, {})) {
            AppScaffold(topLevelNavigation = true, topBar = { AppTopAppBar("主题控件验证") }, snackbarHost = { AppSnackbarHost(feedback) }) { padding ->
                LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AppText(stringResource(AppThemeCatalog.option(style).titleRes)) }
                    item { FlowRow { AppThemeCatalog.options.forEach { option -> AppTextButton(stringResource(option.titleRes), { style = option.style }) } } }
                    item { Row { AppTextButton(if (dark) "浅色" else "深色", { dark = !dark }); AppText("确认 $confirmations / 窗口关闭 $dismissals") } }
                    item { FlowRow { AppButton("按钮", { confirmations++ }); AppButton("禁用", {}, enabled = false); AppTextButton("文字操作", { confirmations++ }) } }
                    item { Row { AppCheckbox(checked, { checked = it }); AppRadioButton(checked, { checked = !checked }); AppSwitch(checked, { checked = it }) } }
                    item { Row { AppCheckbox(checked, null, enabled = false); AppRadioButton(checked, null, enabled = false); AppSwitch(checked, null, enabled = false) } }
                    item { AppSearchField(query, { query = it }, "搜索 / Search / البحث") }
                    item { AppSettingsRow("很长的设置标题，用于检查放大字体后的自然换行", summary = "说明文字 / Text summary / テキスト", titleMaxLines = Int.MAX_VALUE, summaryMaxLines = Int.MAX_VALUE, onClick = { checked = !checked }, trailing = { AppSwitch(checked, null) }) }
                    item { AppLocaleChoiceRow("中文（简体，中国） / English / Português do Brasil", "选择状态与右侧收藏操作分别响应", checked, { checked = !checked }, titleMaxLines = Int.MAX_VALUE, trailingAction = { AppToolbarIconButton(AppSymbol.Pin, "收藏", { confirmations++ }) }) }
                    item { Row { AppCircularProgressIndicator(); AppLinearProgressIndicator(Modifier.weight(1f)) } }
                    if (style == AppThemeStyle.MATERIAL_LOLLIPOP) item { Column { listOf(0f, .5f, 1f).forEach { LollipopProgress(horizontal = true, progress = it) } } }
                    if (style == AppThemeStyle.ECLAIR) item { Column { listOf(0f, .5f, 1f).forEach {
                        ing.fuyaoskyrocket.applocale.ui.designsystem.eclair.EclairProgress(horizontal = true, progress = it)
                    } } }
                    item { FlowRow { AppButton("确认窗口", { alert = true }); AppButton("长目录", { picker = true }); Box { AppButton("菜单", { menu = true }); AppDropdownMenu(menu, { menu = false }, listOf(AppDropdownItem("菜单操作", checked) { menu = false; checked = !checked })) }; AppButton("消息", { scope.launch { feedback.showSnackbar("这是一条控件验证消息") } }) } }
                    items(20) { AppSettingsRow("滚动行 ${it + 1}", summary = "用于检查行高、连续背景和滚动状态") }
                }
            }
            AppAlertDialog(alert, "确认操作", "确认与取消只响应一次；按返回或点窗口外部应取消。", "确认", { confirmations++; alert = false }, "取消", { alert = false }, { dismissals++ })
            AppModalBottomSheet(picker, { picker = false }, title = "可滚动目录", onDismissFinished = { dismissals++ }) {
                LazyColumn(Modifier.heightIn(max = 400.dp)) { items(40) { index -> AppLocaleChoiceRow("目录选项 $index", null, index == 1, { picker = false }) } }
            }
        }
    }
}
