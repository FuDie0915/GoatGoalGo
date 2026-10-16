package com.goatgoalgo.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.goatgoalgo.app.data.TodoStatus
import com.goatgoalgo.app.preferences.AppPreferences
import com.goatgoalgo.app.preferences.ColorRole
import com.goatgoalgo.app.preferences.PaletteId
import com.goatgoalgo.app.preferences.ThemeMode

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private data class PaletteTokens(
    val lightBackground: Color,
    val lightSurface: Color,
    val lightPrimary: Color,
    val lightAccent: Color,
    val darkBackground: Color,
    val darkSurface: Color,
    val darkPrimary: Color,
    val darkAccent: Color,
)

private fun paletteTokens(palette: PaletteId): PaletteTokens = when (palette) {
    PaletteId.OCEAN -> PaletteTokens(
        Color(0xFFF3F7FF), Color(0xFFFFFFFF), Color(0xFF3569B8), Color(0xFF5B8DEF),
        Color(0xFF101722), Color(0xFF182333), Color(0xFF9DBBFF), Color(0xFF76A5FF),
    )
    PaletteId.FOREST -> PaletteTokens(
        Color(0xFFF3FAF5), Color(0xFFFFFFFF), Color(0xFF2F7D4A), Color(0xFF5DBB78),
        Color(0xFF0D1B13), Color(0xFF14271B), Color(0xFF8ED8A4), Color(0xFF6ACD8A),
    )
    PaletteId.VIOLET -> PaletteTokens(
        Color(0xFFF8F4FF), Color(0xFFFFFFFF), Color(0xFF7047A8), Color(0xFF9A70D6),
        Color(0xFF18121F), Color(0xFF271B33), Color(0xFFD3B5FF), Color(0xFFB68BEF),
    )
    PaletteId.SUNSET -> PaletteTokens(
        Color(0xFFFFF7F0), Color(0xFFFFFFFF), Color(0xFFB45C2B), Color(0xFFE18A4D),
        Color(0xFF21150F), Color(0xFF332018), Color(0xFFFFB887), Color(0xFFFF9B59),
    )
    PaletteId.ROSE -> PaletteTokens(
        Color(0xFFFFF5F8), Color(0xFFFFFFFF), Color(0xFFB6496A), Color(0xFFE17D9A),
        Color(0xFF211116), Color(0xFF341B23), Color(0xFFFFB4C8), Color(0xFFFF91AE),
    )
    PaletteId.SLATE -> PaletteTokens(
        Color(0xFFF4F6F8), Color(0xFFFFFFFF), Color(0xFF4E5D6C), Color(0xFF788B9E),
        Color(0xFF111417), Color(0xFF1B2025), Color(0xFFB5C5D5), Color(0xFF9BAFC1),
    )
}

private fun PaletteTokens.background(dark: Boolean): Color =
    if (dark) darkBackground else lightBackground

private fun PaletteTokens.surface(dark: Boolean): Color =
    if (dark) darkSurface else lightSurface

private fun PaletteTokens.primary(dark: Boolean): Color =
    if (dark) darkPrimary else lightPrimary

private fun PaletteTokens.accent(dark: Boolean): Color =
    if (dark) darkAccent else lightAccent

@Composable
fun GoatGoalGoTheme(
    preferences: AppPreferences,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (preferences.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val paletteFor = { role: ColorRole ->
        paletteTokens(preferences.overrides[role] ?: preferences.globalPalette)
    }
    val background = paletteFor(ColorRole.BACKGROUND).background(dark)
    val surface = paletteFor(ColorRole.SURFACE).surface(dark)
    val primary = paletteFor(ColorRole.PRIMARY).primary(dark)
    val accent = paletteFor(ColorRole.ACCENT).accent(dark)
    val outline = if (dark) Color(0xFF314052) else Color(0xFFD5DCE6)
    val outlineVariant = if (dark) Color(0xFF263140) else Color(0xFFE7EDF4)
    val scheme = if (dark) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color(0xFF101010),
            secondary = accent,
            onSecondary = Color(0xFF101010),
            background = background,
            onBackground = Color(0xFFF2F4F7),
            surface = surface,
            onSurface = Color(0xFFF2F4F7),
            surfaceVariant = surface,
            surfaceContainer = surface,
            surfaceContainerHigh = surface,
            onSurfaceVariant = Color(0xFFC6CDD5),
            outline = outline,
            outlineVariant = outlineVariant,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            secondary = accent,
            onSecondary = Color.White,
            background = background,
            onBackground = Color(0xFF1A1C1E),
            surface = surface,
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = Color(0xFFF7F8FA),
            surfaceContainer = surface,
            surfaceContainerHigh = surface,
            onSurfaceVariant = Color(0xFF5D6268),
            outline = outline,
            outlineVariant = outlineVariant,
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}

fun paletteSwatch(palette: PaletteId, dark: Boolean): Color =
    paletteTokens(palette).primary(dark)

fun todoStatusColor(status: TodoStatus, dark: Boolean): Color = when (status) {
    TodoStatus.ACTIVE -> if (dark) Color(0xFFB6BEC8) else Color(0xFF69727D)
    TodoStatus.COMPLETED -> if (dark) Color(0xFF8DD59B) else Color(0xFF2E7D32)
    TodoStatus.FAILED -> if (dark) Color(0xFFFF9B9B) else Color(0xFFC62828)
    TodoStatus.ABANDONED -> if (dark) Color(0xFF9EC4FF) else Color(0xFF1565C0)
}

fun todoStatusLabel(status: TodoStatus): String = when (status) {
    TodoStatus.ACTIVE -> "进行中"
    TodoStatus.COMPLETED -> "完成"
    TodoStatus.FAILED -> "失败"
    TodoStatus.ABANDONED -> "放弃"
}
