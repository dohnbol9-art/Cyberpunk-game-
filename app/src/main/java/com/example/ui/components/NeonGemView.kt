package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun NeonGemView(
  gem: Gem,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "gem_glow")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "spin"
  )

  val scale = if (isSelected) pulseScale * 1.08f else 1.0f

  Box(
    modifier = modifier
      .scale(scale)
      .clip(RoundedCornerShape(8.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = gem.type.color)
      ) { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
      val w = size.width
      val h = size.height
      val center = Offset(w / 2f, h / 2f)
      val radius = minOf(w, h) * 0.42f

      // Draw specialized glitched tile
      if (gem.special == SpecialType.CORRUPTED) {
        drawCorruptedNode(center, radius)
        return@Canvas
      }

      // Draw Color Nova
      if (gem.special == SpecialType.COLOR_NOVA) {
        drawColorNova(center, radius, rotation)
        return@Canvas
      }

      // Draw Base Gem Shape based on GemType
      drawGemShape(gem.type, center, radius)

      // Draw Selected Glow
      if (isSelected) {
        drawCircle(
          color = Color.White.copy(alpha = 0.4f),
          radius = radius * 1.15f,
          style = Stroke(width = 3.dp.toPx())
        )
      }

      // Draw Special Modifiers
      when (gem.special) {
        SpecialType.BEAM_HORIZONTAL -> drawBeamIndicator(center, radius, isHorizontal = true)
        SpecialType.BEAM_VERTICAL -> drawBeamIndicator(center, radius, isHorizontal = false)
        SpecialType.NEON_BOMB -> drawBombIndicator(center, radius, rotation)
        else -> Unit
      }
    }
  }
}

private fun DrawScope.drawGemShape(type: GemType, center: Offset, radius: Float) {
  val baseColor = type.color
  val path = Path()

  when (type) {
    GemType.CYAN_PRISM -> {
      // Octagon / Prism
      val step = Math.PI * 2 / 8
      for (i in 0 until 8) {
        val angle = i * step - Math.PI / 8
        val x = center.x + (radius * kotlin.math.cos(angle)).toFloat()
        val y = center.y + (radius * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      path.close()
    }
    GemType.MAGENTA_HEART -> {
      // Diamond Core
      path.moveTo(center.x, center.y - radius * 1.1f)
      path.lineTo(center.x + radius, center.y)
      path.lineTo(center.x, center.y + radius * 1.1f)
      path.lineTo(center.x - radius, center.y)
      path.close()
    }
    GemType.AMBER_STAR -> {
      // 5-Point Star
      val points = 5
      val innerRadius = radius * 0.52f
      for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius * 1.05f else innerRadius
        val angle = i * Math.PI / points - Math.PI / 2
        val x = center.x + (r * kotlin.math.cos(angle)).toFloat()
        val y = center.y + (r * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      path.close()
    }
    GemType.ACID_EMERALD -> {
      // Hexagon Shield
      val step = Math.PI * 2 / 6
      for (i in 0 until 6) {
        val angle = i * step - Math.PI / 6
        val x = center.x + (radius * kotlin.math.cos(angle)).toFloat()
        val y = center.y + (radius * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      path.close()
    }
    GemType.VIOLET_ORB -> {
      // Glowing Circle
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color.White.copy(alpha = 0.9f), baseColor, baseColor.copy(alpha = 0.6f)),
          center = center,
          radius = radius
        ),
        radius = radius,
        center = center
      )
      drawCircle(
        color = baseColor,
        radius = radius,
        center = center,
        style = Stroke(width = 2.5f)
      )
      return
    }
    GemType.ORANGE_FLAME -> {
      // Triangular Spark
      path.moveTo(center.x, center.y - radius * 1.15f)
      path.lineTo(center.x + radius * 1.05f, center.y + radius * 0.85f)
      path.lineTo(center.x - radius * 1.05f, center.y + radius * 0.85f)
      path.close()
    }
  }

  // Draw Glow & Fill
  drawPath(
    path = path,
    brush = Brush.radialGradient(
      colors = listOf(Color.White.copy(alpha = 0.85f), baseColor, baseColor.copy(alpha = 0.7f)),
      center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
      radius = radius * 1.2f
    )
  )

  // Neon Edge Stroke
  drawPath(
    path = path,
    color = baseColor,
    style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
  )

  // Subtle inner specular glint
  drawCircle(
    color = Color.White.copy(alpha = 0.65f),
    radius = radius * 0.22f,
    center = Offset(center.x - radius * 0.28f, center.y - radius * 0.28f)
  )
}

private fun DrawScope.drawColorNova(center: Offset, radius: Float, rotation: Float) {
  val rainbowColors = listOf(NeonCyan, NeonMagenta, NeonAmber, NeonGreen, NeonPurple, NeonCyan)
  drawCircle(
    brush = Brush.sweepGradient(rainbowColors, center = center),
    radius = radius * 1.1f,
    center = center
  )
  drawCircle(
    color = Color.White,
    radius = radius * 0.5f,
    center = center
  )
  drawCircle(
    color = Color.White,
    radius = radius * 1.15f,
    center = center,
    style = Stroke(width = 3f)
  )
}

private fun DrawScope.drawCorruptedNode(center: Offset, radius: Float) {
  // Glitched Diamond
  val path = Path().apply {
    moveTo(center.x, center.y - radius)
    lineTo(center.x + radius, center.y)
    lineTo(center.x, center.y + radius)
    lineTo(center.x - radius, center.y)
    close()
  }
  drawPath(
    path = path,
    brush = Brush.linearGradient(
      colors = listOf(Color(0xFF4A0E4E), Color(0xFF1F0322)),
      start = Offset(center.x - radius, center.y - radius),
      end = Offset(center.x + radius, center.y + radius)
    )
  )
  drawPath(
    path = path,
    color = CyberCorrupt,
    style = Stroke(width = 2.5f)
  )

  // Glitch cross
  drawLine(
    color = Color(0xFFFF0055),
    start = Offset(center.x - radius * 0.5f, center.y - radius * 0.5f),
    end = Offset(center.x + radius * 0.5f, center.y + radius * 0.5f),
    strokeWidth = 3f
  )
  drawLine(
    color = Color(0xFFFF0055),
    start = Offset(center.x - radius * 0.5f, center.y + radius * 0.5f),
    end = Offset(center.x + radius * 0.5f, center.y - radius * 0.5f),
    strokeWidth = 3f
  )
}

private fun DrawScope.drawBeamIndicator(center: Offset, radius: Float, isHorizontal: Boolean) {
  val strokeWidth = 3.5f
  if (isHorizontal) {
    drawLine(
      color = Color.White,
      start = Offset(center.x - radius * 0.85f, center.y),
      end = Offset(center.x + radius * 0.85f, center.y),
      strokeWidth = strokeWidth
    )
    // Laser arrows
    drawLine(Color.White, Offset(center.x - radius * 0.85f, center.y), Offset(center.x - radius * 0.5f, center.y - 4f), strokeWidth = 2f)
    drawLine(Color.White, Offset(center.x - radius * 0.85f, center.y), Offset(center.x - radius * 0.5f, center.y + 4f), strokeWidth = 2f)
    drawLine(Color.White, Offset(center.x + radius * 0.85f, center.y), Offset(center.x + radius * 0.5f, center.y - 4f), strokeWidth = 2f)
    drawLine(Color.White, Offset(center.x + radius * 0.85f, center.y), Offset(center.x + radius * 0.5f, center.y + 4f), strokeWidth = 2f)
  } else {
    drawLine(
      color = Color.White,
      start = Offset(center.x, center.y - radius * 0.85f),
      end = Offset(center.x, center.y + radius * 0.85f),
      strokeWidth = strokeWidth
    )
    // Laser arrows
    drawLine(Color.White, Offset(center.x, center.y - radius * 0.85f), Offset(center.x - 4f, center.y - radius * 0.5f), strokeWidth = 2f)
    drawLine(Color.White, Offset(center.x, center.y - radius * 0.85f), Offset(center.x + 4f, center.y - radius * 0.5f), strokeWidth = 2f)
    drawLine(Color.White, Offset(center.x, center.y + radius * 0.85f), Offset(center.x - 4f, center.y + radius * 0.5f), strokeWidth = 2f)
    drawLine(Color.White, Offset(center.x, center.y + radius * 0.85f), Offset(center.x + 4f, center.y + radius * 0.5f), strokeWidth = 2f)
  }
}

private fun DrawScope.drawBombIndicator(center: Offset, radius: Float, rotation: Float) {
  drawCircle(
    color = NeonMagenta,
    radius = radius * 0.7f,
    center = center,
    style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
  )
  drawCircle(
    color = Color.White,
    radius = radius * 0.28f,
    center = center
  )
}
