package ing.fuyaoskyrocket.applocale.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.components.AppLanguagePreference
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloAppTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCheckbox
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppCircularProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppDivider
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppFilledTonalButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppLinearProgressIndicator
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppLocaleChoiceRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppOutlinedButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppRadioButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSearchField
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSettingsRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSwitch
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppTextButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.LocalHoloTextColors

/**
 * Round-9 control gallery (040 bridge + 041 full controls), driving the REAL
 * App* dispatchers inside HoloAppTheme so every state the matrix C01—C13/C17
 * lists is checkable on a debug install. Development artifact only.
 */
class HoloGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HoloAppTheme {
                Gallery()
            }
        }
    }
}

@Composable
private fun Gallery() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Section("AppText roles")
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText("pageTitle 18sp", style = AppUiTheme.textStyles.pageTitle)
                AppText("itemTitle 18sp", style = AppUiTheme.textStyles.itemTitle)
                AppText("body 16sp")
                AppText(
                    "metadata 14sp dim",
                    style = AppUiTheme.textStyles.metadata,
                    color = AppUiTheme.palette.muted,
                )
            }
        }
        item {
            Section("Buttons")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AppButton("Standard", onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextButton("Borderless", onClick = {})
                AppOutlinedButton("Outlined→std", onClick = {})
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppFilledTonalButton("Tonal→std", onClick = {})
                AppButton("Disabled", onClick = {}, enabled = false)
            }
        }
        item {
            Section("Icon button + progress")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppIconButton(onClick = {}) {}
                AppCircularProgressIndicator()
                AppLinearProgressIndicator(modifier = Modifier.padding(top = 18.dp).weight(1f))
            }
        }
        item {
            Section("Checkbox / Radio / Switch")
            var checked by remember { mutableStateOf(true) }
            var radio by remember { mutableStateOf(false) }
            var switched by remember { mutableStateOf(true) }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AppCheckbox(checked = checked, onCheckedChange = { checked = it })
                AppCheckbox(checked = !checked, onCheckedChange = null)
                AppCheckbox(checked = true, onCheckedChange = {}, enabled = false)
                AppRadioButton(selected = radio, onClick = { radio = !radio })
                AppRadioButton(selected = !radio, onClick = null)
                AppRadioButton(selected = true, onClick = {}, enabled = false)
            }
            AppSwitch(checked = switched, onCheckedChange = { switched = it })
            AppSwitch(checked = false, onCheckedChange = null, enabled = false)
        }
        item {
            Section("Search field")
            var query by remember { mutableStateOf("") }
            AppSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search apps",
            )
        }
        item { AppDivider() }
        item {
            Section("Settings row")
            AppSettingsRow(title = "Single line setting", onClick = {})
            AppSettingsRow(
                title = "Two line setting",
                summary = "Summary text in the dim foreground colour",
                onClick = {},
            )
            AppSettingsRow(
                title = "Disabled row",
                summary = "Not clickable",
                onClick = null,
                titleColor = LocalHoloTextColors.current.primaryDisabled,
            )
        }
        item {
            Section("Locale choice row")
            var selected by remember { mutableStateOf("A") }
            Column {
                AppLocaleChoiceRow(
                    title = "简体中文 (中国)",
                    subtitle = "zh-CN · 简体中文",
                    selected = selected == "A",
                    onSelect = { selected = "A" },
                )
                AppLocaleChoiceRow(
                    title = "English (United States)",
                    subtitle = "en-US · English",
                    selected = selected == "B",
                    onSelect = { selected = "B" },
                )
            }
        }
    }
}

@Composable
private fun Section(text: String) {
    AppText(
        text,
        style = AppUiTheme.textStyles.metadata,
        color = AppUiTheme.palette.muted,
    )
}
