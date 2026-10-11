package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.engine.ParticleManager
import com.example.ui.theme.*

@Composable
fun NeonEffectsCanvas(
  particleManager: ParticleManager,
  modifier: Modifier = Modifier
) {
  var lastFrameTime by remember { mutableLongStateOf(0L) }

  LaunchedEffect(Unit) {
    while (true) {
      withFrameMillis { frameTime ->
        if (lastFrameTime > 0L) {
          val dt = ((frameTime - lastFrameTime) / 1000f).coerceIn(0.001f, 0.05f)
          particleManager.update(dt)
        }
        lastFrameTime = frameTime
      }
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    // 1. Draw Lasers
    for (laser in particleManager.lasers) {
      val progress = laser.progress
      val alpha = (1f - progress).coerceIn(0f, 1f)
      val coreWidth = 8f * (1f - progress * 0.5f)
      val glowWidth = 28f * (1f - progress * 0.5f)

      // Outer Glow
      drawLine(
        color = laser.color.copy(alpha = alpha * 0.5f),
        start = laser.start,
        end = laser.end,
        strokeWidth = glowWidth,
        cap = StrokeCap.Round
      )
      // Bright Center Core
      drawLine(
        color = Color.White.copy(alpha = alpha),
        start = laser.start,
        end = laser.end,
        strokeWidth = coreWidth,
        cap = StrokeCap.Round
      )
    }

    // 2. Draw Shockwaves
    for (ring in particleManager.shockwaves) {
      drawCircle(
        color = ring.color.copy(alpha = ring.alpha),
        radius = ring.currentRadius,
        center = ring.center,
        style = Stroke(width = 6f * ring.alpha)
      )
      drawCircle(
        color = Color.White.copy(alpha = ring.alpha * 0.8f),
        radius = ring.currentRadius * 0.95f,
        center = ring.center,
        style = Stroke(width = 2.5f * ring.alpha)
      )
    }

    // 3. Draw Neon Particles
    for (p in particleManager.particles) {
      // Glow circle
      drawCircle(
        color = p.color.copy(alpha = p.alpha * 0.45f),
        radius = p.size * 1.8f,
        center = Offset(p.x, p.y)
      )
      // Core particle
      drawCircle(
        color = Color.White.copy(alpha = p.alpha),
        radius = p.size * 0.7f,
        center = Offset(p.x, p.y)
      )
    }

    // 4. Draw Floating Combat Text
    val paint = android.graphics.Paint().apply {
      isAntiAlias = true
      textAlign = android.graphics.Paint.Align.CENTER
      typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    val native = drawContext.canvas.nativeCanvas
    for (txt in particleManager.floatingTexts) {
      paint.textSize = if (txt.isCrit) 54f else 42f
      paint.color = txt.color.toArgb()
      paint.alpha = (txt.alpha * 255).toInt().coerceIn(0, 255)
      paint.setShadowLayer(16f, 0f, 0f, txt.color.toArgb())

      native.drawText(txt.text, txt.x, txt.y, paint)
    }
  }
}
