package com.manekelsa.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Saffron = Color(0xFFE8860A)
    val SaffronLight = Color(0xFFFFA733)
    val Teal = Color(0xFF00796B)
    val Charcoal = Color(0xFF1A1A2E)
    val MidGray = Color(0xFF6B7080)
    val LightGray = Color(0xFFE8EAF0)
    val OffWhite = Color(0xFFF9F7F4)
    val Available = Color(0xFF2E7D32)
    val Unavailable = Color(0xFFB71C1C)
    val Verified = Color(0xFF1565C0)
    val StarYellow = Color(0xFFFFC107)
}

private val LightColors = lightColorScheme(
    primary = AppColors.Saffron,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    secondary = AppColors.Teal,
    onSecondary = Color.White,
    background = AppColors.OffWhite,
    surface = Color.White,
    onBackground = AppColors.Charcoal,
    onSurface = AppColors.Charcoal,
    surfaceVariant = AppColors.LightGray,
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = AppColors.SaffronLight,
    onPrimary = Color.Black,
    secondary = AppColors.Teal,
    background = AppColors.Charcoal,
    surface = Color(0xFF2D2D44),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun ManeKelsaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
