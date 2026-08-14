package ing.fuyaoskyrocket.applocale.ui.components

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

/**
 * In-app front end for Android 13's per-app language preference. The framework
 * persists and synchronizes the chosen locale with the system Settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLanguagePreference(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val localeManager = remember(context) {
        context.getSystemService(LocaleManager::class.java)
    }
    var showPicker by remember { mutableStateOf(false) }
    var currentTag by remember(localeManager) {
        mutableStateOf(localeManager.applicationLocales.toLanguageTags().firstLanguageTagOrNull())
    }
    val options = appLanguageOptions()

    ListItem(
        modifier = modifier.clickable { showPicker = true },
        headlineContent = { Text(stringResource(R.string.app_language)) },
        supportingContent = {
            Text(options.firstOrNull { it.localeTag == currentTag }?.label ?: currentTag.orEmpty())
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )

    if (showPicker) {
        ModalBottomSheet(onDismissRequest = { showPicker = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppSpacing.xl,
                        vertical = AppSpacing.sm,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.choose_app_language),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(
                        start = AppSpacing.sm,
                        top = AppSpacing.sm,
                        end = AppSpacing.sm,
                        bottom = AppSpacing.lg,
                    ),
                )
                options.forEach { option ->
                    val selected = option.localeTag == currentTag
                    val contentColor = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = AppLayout.standardListItemMinHeight)
                            .clip(MaterialTheme.shapes.large)
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = {
                                    showPicker = false
                                    currentTag = option.localeTag
                                    localeManager.applicationLocales = option.localeTag
                                        ?.let(LocaleList::forLanguageTags)
                                        ?: LocaleList.getEmptyLocaleList()
                                },
                            ),
                        headlineContent = {
                            Text(
                                text = option.label,
                                color = contentColor,
                            )
                        },
                        trailingContent = {
                            RadioButton(selected = selected, onClick = null)
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                Color.Transparent
                            },
                        ),
                    )
                }
            }
        }
    }
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

private fun String.firstLanguageTagOrNull(): String? =
    substringBefore(',').takeIf(String::isNotBlank)
