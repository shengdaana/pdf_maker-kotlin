package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFFEF5350),
  onPrimary = Color(0xFF5F0004),
  primaryContainer = Color(0xFF8E0000),
  onPrimaryContainer = Color(0xFFFFCDD2),
  secondary = Color(0xFFE0E0E0),
  onSecondary = Color(0xFF212121),
  background = Color(0xFF121212),
  surface = Color(0xFF1E1E1E),
  surfaceVariant = Color(0xFF2C2C2C),
  onBackground = Color(0xFFE0E0E0),
  onSurface = Color(0xFFE0E0E0),
  outline = Color(0xFF757575)
)

private val LightColorScheme = lightColorScheme(
  primary = PdfRedPrimary,
  onPrimary = Color.White,
  primaryContainer = PdfRedContainer,
  onPrimaryContainer = PdfOnRedContainer,
  secondary = PdfSlate700,
  onSecondary = Color.White,
  background = PdfSlate50,
  surface = Color.White,
  surfaceVariant = PdfSlate100,
  onBackground = PdfSlate900,
  onSurface = PdfSlate900,
  outline = Color(0xFFCBD5E1)
)

private val HighContrastScheme = lightColorScheme(
  primary = ElderHighContrastRed,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFCDD2),
  onPrimaryContainer = ElderBlack,
  secondary = ElderBlack,
  onSecondary = Color.White,
  background = Color.White,
  surface = Color.White,
  surfaceVariant = Color(0xFFF5F5F5),
  onBackground = ElderBlack,
  onSurface = ElderBlack,
  outline = ElderBlack
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  highContrast: Boolean = false,
  dynamicColor: Boolean = false, // Keep consistent PDF red brand by default
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val colorScheme = when {
    highContrast -> HighContrastScheme
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
