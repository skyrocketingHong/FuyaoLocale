package ing.fuyaoskyrocket.applocale.ui.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/** Uses the native M3 type scale with Fuyao's emphasized title and action weights. */
private val BaseTypography = Typography()

/** Matches the emphasized title and action hierarchy used by Fuyao Color. */
val AppTypography = Typography(
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = BaseTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)
