package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonCyberColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = Color.Black,
  primaryContainer = CyberSurfaceVariant,
  onPrimaryContainer = NeonCyan,
  secondary = NeonMagenta,
  onSecondary = Color.White,
  secondaryContainer = CyberBorder,
  onSecondaryContainer = NeonMagenta,
  tertiary = NeonAmber,
  onTertiary = Color.Black,
  background = CyberDarkBg,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CyberBorder,
  outlineVariant = CyberBorderGlow
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = NeonCyberColorScheme,
    typography = Typography,
    content = content
  )
}
