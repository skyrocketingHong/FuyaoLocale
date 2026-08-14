package ing.fuyaoskyrocket.applocale.ui.appinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.ui.components.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing

/**
 * App identity section: icon, label, package name, current language.
 */
@Composable
fun AppIdentityHeader(
    packageName: String,
    label: String,
    currentLocaleTag: String?,
    systemLocaleTag: String,
    iconLoader: AppIconLoader,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                packageName = packageName,
                iconLoader = iconLoader,
                modifier = Modifier.size(AppLayout.appHeaderIconSize),
            )
            Column(
                modifier = Modifier
                    .padding(start = AppSpacing.lg)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val currentLang = currentLocaleTag ?: if (systemLocaleTag.isBlank()) {
                    stringResource(R.string.system_default)
                } else {
                    stringResource(R.string.system_default_with_locale, systemLocaleTag)
                }
                Text(
                    text = currentLang,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
