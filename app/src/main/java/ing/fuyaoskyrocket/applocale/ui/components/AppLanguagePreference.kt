package ing.fuyaoskyrocket.applocale.ui.components

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.LocalAppearanceRequester
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppLocaleChoiceRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppModalBottomSheet
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSettingsRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import java.util.Locale

/**
 * In-app front end for Android 13's per-app language preference. The framework
 * persists and synchronizes the chosen locale with the system Settings page.
 */
@Composable
fun AppLanguagePreference(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showPicker by remember { mutableStateOf(false) }
    // Read once at first display; refreshed again before the sheet opens and on
    // ON_RESUME so external changes (system Settings) reach the summary without
    // polling or per-recomposition reads.
    var currentTag by remember {
        mutableStateOf(readApplicationLocaleTag(context))
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentTag = readApplicationLocaleTag(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val appearanceRequester = LocalAppearanceRequester.current
    fun applyOption(option: AppLanguageOption) {
        // Close first; the real write happens from the appearance transaction
        // once this sheet has actually finished closing, so the page fades out
        // behind a closed window instead of a half-open one.
        if (!showPicker) return
        showPicker = false
        val requester = appearanceRequester
        if (requester != null) {
            requester.requestAppLocale(option.localeTag)
        } else {
            currentTag = option.localeTag
            context.getSystemService(LocaleManager::class.java)?.let { manager ->
                manager.applicationLocales = option.localeTag
                    ?.let(LocaleList::forLanguageTags)
                    ?: LocaleList.getEmptyLocaleList()
            }
        }
    }

    val options = appLanguageOptions()
    val selectedOption = options.firstOrNull { option -> matchesOption(currentTag, option) }
    val summary = selectedOption?.label ?: currentTag.orEmpty()

    AppSettingsRow(
        title = stringResource(R.string.app_language),
        modifier = modifier,
        summary = summary,
        onClick = {
            currentTag = readApplicationLocaleTag(context)
            showPicker = true
        },
        trailing = {
            AppIcon(
                imageVector = AppSymbolVector(AppSymbol.Forward),
                contentDescription = null,
                tint = AppUiTheme.palette.muted,
            )
        },
    )

    AppModalBottomSheet(
        visible = showPicker,
        onDismiss = { showPicker = false },
        onDismissFinished = {
            // Only now may the pending language fade-out begin.
            appearanceRequester?.confirmAppLocaleSheetClosed()
        },
        title = stringResource(R.string.choose_app_language),
    ) {
        AppLanguageSheetContent(
            options = options,
            selectedOption = selectedOption,
            systemLocaleTag = rememberSystemLocaleTag(),
            onOptionClick = { option -> applyOption(option) },
        )
    }
}

/**
 * The five fixed options as one continuous list (round-7 032): follow-system
 * first, then the four supported languages in their declared order. Same shared
 * row, badge and frame as the directory lists — rows take the 4dp outer margin,
 * never a second 16dp inset — with no grouped cards or dividers between them.
 * The list wraps its content and only scrolls past 70% of the screen height.
 */
@Composable
private fun AppLanguageSheetContent(
    options: List<AppLanguageOption>,
    selectedOption: AppLanguageOption?,
    systemLocaleTag: String,
    onOptionClick: (AppLanguageOption) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f),
        contentPadding = PaddingValues(
            top = AppSpacing.sm,
            bottom = AppSpacing.xl,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(
            items = options,
            key = { _, option -> option.localeTag ?: "follow_system" },
            contentType = { _, _ -> "locale" },
        ) { index, option ->
            val badgeTag = option.localeTag ?: systemLocaleTag
            AppLocaleChoiceRow(
                title = option.label,
                subtitle = badgeTag.takeIf { it.isNotBlank() }?.let { tag ->
                    "$tag · ${localeAutonym(tag)}"
                },
                selected = selectedOption == option,
                onSelect = { onOptionClick(option) },
                leading = {
                    // Follow-system shows the real system locale's mark — a
                    // display aid only, never a fifth supported language.
                    LocaleBadge(
                        languageTag = badgeTag,
                        preferRegion = true,
                    )
                },
                modifier = Modifier
                    .readableContentWidth()
                    .padding(horizontal = AppLayout.localeRowOuterMargin),
            )
            if (index == 0 && options.size > 1) {
                Spacer(Modifier.height(AppSpacing.sm))
            }
        }
    }
}

/** The tag's own name for itself, cached per tag — never a hardcoded translation. */
@Composable
private fun localeAutonym(tag: String): String = remember(tag) {
    val locale = Locale.forLanguageTag(tag)
    locale.getDisplayName(locale)
}

@Composable
private fun appLanguageOptions(): List<AppLanguageOption> = listOf(
    AppLanguageOption(null, stringResource(R.string.follow_system_language)),
    AppLanguageOption("zh-CN", stringResource(R.string.language_simplified_chinese)),
    AppLanguageOption("en", stringResource(R.string.language_english)),
    AppLanguageOption("ja", stringResource(R.string.language_japanese)),
    AppLanguageOption("pt-BR", stringResource(R.string.language_portuguese_brazil)),
)

private data class AppLanguageOption(
    val localeTag: String?,
    val label: String,
)

private fun readApplicationLocaleTag(context: Context): String? =
    context.getSystemService(LocaleManager::class.java)
        ?.applicationLocales
        ?.toLanguageTags()
        ?.firstLanguageTagOrNull()

/** Known in-app resource aliases shown as their canonical option tag. */
private val displayAliases: Map<String, String> = mapOf(
    "zh-Hans" to "zh-CN",
    "zh-Hans-CN" to "zh-CN",
)

/**
 * Display-only selection match: exact (canonical BCP-47, case-insensitive) first,
 * then the declared aliases; a language-only option also absorbs that language's
 * regional variants. Never rewrites the stored system value, and an unknown tag
 * matches nothing (shown raw in the entry summary instead).
 */
private fun matchesOption(currentTag: String?, option: AppLanguageOption): Boolean {
    if (currentTag == null || option.localeTag == null) {
        return currentTag == null && option.localeTag == null
    }
    val current = canonicalTag(currentTag)
    val target = canonicalTag(option.localeTag)
    return current == target ||
        displayAliases[current] == target ||
        (target.length == 2 && current.startsWith("$target-"))
}

private fun canonicalTag(tag: String): String = Locale.forLanguageTag(tag).toLanguageTag()

private fun String.firstLanguageTagOrNull(): String? =
    substringBefore(',').takeIf(String::isNotBlank)
