package com.antigravity.thai24keyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkGrayBackground = Color(0xFF1E2022)
val DarkKeyBackground = Color(0xFF2C2F33)
val DarkKeyActive = Color(0xFF40444B)
val DarkTextPrimary = Color(0xFFF0F2F5)
val DarkTextSecondary = Color(0xFF9BA1A6)

val LightGrayBackground = Color(0xFFD6DADC)
val LightKeyBackground = Color(0xFFFFFFFF)
val LightKeyActive = Color(0xFFE8ECEF)
val LightTextPrimary = Color(0xFF1F2328)
val LightTextSecondary = Color(0xFF656D76)

val AccentBlue = Color(0xFF007AFF)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    background = DarkGrayBackground,
    surface = DarkKeyBackground,
    onPrimary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    background = LightGrayBackground,
    surface = LightKeyBackground,
    onPrimary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun Thai24KeyboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
