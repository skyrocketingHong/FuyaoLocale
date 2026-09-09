package ing.fuyaoskyrocket.applocale.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloCheckbox
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloCircularProgress
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloCompoundGlyph
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloControlState
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloAsset
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloRadioButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloSwitch
import ing.fuyaoskyrocket.applocale.ui.designsystem.holo.HoloText
import ing.fuyaoskyrocket.applocale.ui.designsystem.kitkat.KitKatWidgetTheme

/** Isolated widget reference: no persisted style or production Provider is substituted. */
class KitKatGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var dark by remember { mutableStateOf(true) }
            var checked by remember { mutableStateOf(true) }
            KitKatWidgetTheme(darkTheme = dark) { palette, type, _ ->
                Column(
                    Modifier.fillMaxSize().background(palette.background)
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HoloText("Android 4.4 KitKat", style = type.pageTitle, color = palette.foreground)
                    HoloButton(if (dark) "Switch to Light" else "Switch to Dark", onClick = { dark = !dark })
                    HoloButton("Button", onClick = {})
                    HoloButton("Disabled", onClick = {}, enabled = false)
                    Row {
                        HoloCheckbox(checked, { checked = it })
                        HoloRadioButton(checked, { checked = !checked })
                        HoloSwitch(checked, { checked = it })
                    }
                    HoloText("Normal · pressed · focused · disabled", style = type.body, color = palette.foreground)
                    for (resource in listOf(HoloAsset.Checkbox, HoloAsset.RadioButton)) {
                        Row {
                            HoloCompoundGlyph(resource, HoloControlState(checked = checked))
                            HoloCompoundGlyph(resource, HoloControlState(checked = checked, pressed = true))
                            HoloCompoundGlyph(resource, HoloControlState(checked = checked, focused = true))
                            HoloCompoundGlyph(resource, HoloControlState(checked = checked, enabled = false))
                        }
                    }
                    HoloCircularProgress()
                }
            }
        }
    }
}
