package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppTheme

private val PurpleColorScheme = lightColorScheme(
  primary = PurplePrimary,
  onPrimary = Color.White,
  primaryContainer = PurpleContainer,
  onPrimaryContainer = PurpleOnContainer,
  secondary = PurpleDark,
  onSecondary = Color.White,
  background = PdfSlate50,
  surface = Color.White,
  surfaceVariant = PdfSlate100,
  onBackground = PdfSlate900,
  onSurface = PdfSlate900,
  outline = Color(0xFFCBD5E1)
)

private val LightGreenColorScheme = lightColorScheme(
  primary = GreenPrimary,
  onPrimary = Color.White,
  primaryContainer = GreenContainer,
  onPrimaryContainer = GreenOnContainer,
  secondary = GreenDark,
  onSecondary = Color.White,
  background = Color(0xFFF9FBFA),
  surface = Color.White,
  surfaceVariant = Color(0xFFE8F5E9),
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  outline = Color(0xFFA7F3D0)
)

private val PinkColorScheme = lightColorScheme(
  primary = PinkPrimary,
  onPrimary = Color.White,
  primaryContainer = PinkContainer,
  onPrimaryContainer = PinkOnContainer,
  secondary = PinkDark,
  onSecondary = Color.White,
  background = Color(0xFFFDF8F9),
  surface = Color.White,
  surfaceVariant = Color(0xFFFCE4EC),
  onBackground = Color(0xFF1E293B),
  onSurface = Color(0xFF1E293B),
  outline = Color(0xFFFBCFE8)
)

private val CobaltBlueColorScheme = lightColorScheme(
  primary = CobaltPrimary,
  onPrimary = Color.White,
  primaryContainer = CobaltContainer,
  onPrimaryContainer = CobaltOnContainer,
  secondary = CobaltDark,
  onSecondary = Color.White,
  background = Color(0xFFF8FAFC),
  surface = Color.White,
  surfaceVariant = Color(0xFFE0E7FF),
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  outline = Color(0xFFBFDBFE)
)

private val ModernDarkColorScheme = darkColorScheme(
  primary = PurpleLight,
  onPrimary = Color(0xFF1E1B4B),
  primaryContainer = Color(0xFF4C1D95),
  onPrimaryContainer = Color(0xFFEDE9FE),
  secondary = Color(0xFFC084FC),
  onSecondary = Color(0xFF1E1B4B),
  background = Color(0xFF0F172A),
  surface = Color(0xFF1E293B),
  surfaceVariant = Color(0xFF334155),
  onBackground = Color(0xFFF8FAFC),
  onSurface = Color(0xFFF8FAFC),
  onSurfaceVariant = Color(0xFFCBD5E1),
  outline = Color(0xFF475569)
)

private val HighContrastDarkColorScheme = darkColorScheme(
  primary = ElderYellowAccent,
  onPrimary = ElderBlack,
  primaryContainer = Color(0xFF333333),
  onPrimaryContainer = Color.White,
  secondary = Color.White,
  onSecondary = ElderBlack,
  background = ElderBlack,
  surface = ElderOledSurface,
  surfaceVariant = ElderOledCard,
  onBackground = Color.White,
  onSurface = Color.White,
  outline = ElderHighContrastBorder
)

@Composable
fun MyApplicationTheme(
  theme: AppTheme = AppTheme.PURPLE,
  content: @Composable () -> Unit
) {
  val colorScheme = when (theme) {
    AppTheme.PURPLE -> PurpleColorScheme
    AppTheme.DARK -> ModernDarkColorScheme
    AppTheme.LIGHT_GREEN -> LightGreenColorScheme
    AppTheme.PINK -> PinkColorScheme
    AppTheme.COBALT_BLUE -> CobaltBlueColorScheme
    AppTheme.HIGH_CONTRAST_DARK -> HighContrastDarkColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

