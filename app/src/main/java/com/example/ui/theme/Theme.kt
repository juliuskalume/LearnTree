package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    secondary = SecondaryBlue,
    background = DeepBlack,
    surface = DeepBlack,
    onBackground = BackgroundColor,
    onSurface = BackgroundColor
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimary,
    secondary = SecondaryBlue,
    background = BackgroundColor,
    surface = BackgroundColor,
    onBackground = OnBackgroundColor,
    onSurface = OnBackgroundColor,
    outline = OutlineGray,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to enforce our custom Duolingo/LearnTree tactile branding exactly
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
