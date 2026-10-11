package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class NeonParticle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  val color: Color,
  var alpha: Float = 1.0f,
  var size: Float = 6f,
  var life: Float = 1.0f,
  val maxLife: Float = 1.0f
) {
  val isDead: Boolean get() = life <= 0f

  fun update(dt: Float) {
    x += vx * dt
    y += vy * dt
    life -= dt
    alpha = (life / maxLife).coerceIn(0f, 1f)
    size = (size * 0.96f).coerceAtLeast(1f)
  }
}

data class LaserEffect(
  val start: Offset,
  val end: Offset,
  val color: Color,
  var life: Float = 0.4f,
  val maxLife: Float = 0.4f,
  val isHorizontal: Boolean
) {
  val isDead: Boolean get() = life <= 0f
  val progress: Float get() = (1f - (life / maxLife)).coerceIn(0f, 1f)
}

data class ShockwaveRing(
  val center: Offset,
  val color: Color,
  val maxRadius: Float = 140f,
  var life: Float = 0.5f,
  val maxLife: Float = 0.5f
) {
  val isDead: Boolean get() = life <= 0f
  val progress: Float get() = (1f - (life / maxLife)).coerceIn(0f, 1f)
  val currentRadius: Float get() = maxRadius * progress
  val alpha: Float get() = (1f - progress).coerceIn(0f, 1f)
}

data class FloatingCombatText(
  val id: String = java.util.UUID.randomUUID().toString(),
  var x: Float,
  var y: Float,
  val text: String,
  val color: Color,
  val isCrit: Boolean = false,
  var life: Float = 1.2f,
  val maxLife: Float = 1.2f
) {
  val isDead: Boolean get() = life <= 0f
  val alpha: Float get() = (life / maxLife).coerceIn(0f, 1f)
  fun update(dt: Float) {
    y -= 38f * dt
    life -= dt
  }
}

class ParticleManager {
  val particles = mutableListOf<NeonParticle>()
  val lasers = mutableListOf<LaserEffect>()
  val shockwaves = mutableListOf<ShockwaveRing>()
  val floatingTexts = mutableListOf<FloatingCombatText>()
  var shakeOffset = Offset.Zero
  private var shakeDuration = 0f
  private var shakeIntensity = 0f

  fun triggerShake(intensity: Float = 12f, duration: Float = 0.3f) {
    shakeIntensity = intensity
    shakeDuration = duration
  }

  fun spawnMatchBurst(x: Float, y: Float, color: Color, count: Int = 16) {
    repeat(count) {
      val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
      val speed = Random.nextFloat() * 180f + 60f
      val vx = kotlin.math.cos(angle) * speed
      val vy = kotlin.math.sin(angle) * speed
      val life = Random.nextFloat() * 0.4f + 0.35f
      particles.add(
        NeonParticle(
          x = x,
          y = y,
          vx = vx,
          vy = vy,
          color = color,
          size = Random.nextFloat() * 6f + 4f,
          life = life,
          maxLife = life
        )
      )
    }
  }

  fun spawnLaser(start: Offset, end: Offset, color: Color, isHorizontal: Boolean) {
    lasers.add(LaserEffect(start, end, color, isHorizontal = isHorizontal))
  }

  fun spawnShockwave(center: Offset, color: Color) {
    shockwaves.add(ShockwaveRing(center, color))
  }

  fun spawnFloatingText(x: Float, y: Float, text: String, color: Color, isCrit: Boolean = false) {
    floatingTexts.add(FloatingCombatText(x = x, y = y, text = text, color = color, isCrit = isCrit))
  }

  fun update(dt: Float) {
    // Update particles
    val pIter = particles.iterator()
    while (pIter.hasNext()) {
      val p = pIter.next()
      p.update(dt)
      if (p.isDead) pIter.remove()
    }

    // Update lasers
    val lIter = lasers.iterator()
    while (lIter.hasNext()) {
      val l = lIter.next()
      l.life -= dt
      if (l.isDead) lIter.remove()
    }

    // Update shockwaves
    val sIter = shockwaves.iterator()
    while (sIter.hasNext()) {
      val s = sIter.next()
      s.life -= dt
      if (s.isDead) sIter.remove()
    }

    // Update floating texts
    val tIter = floatingTexts.iterator()
    while (tIter.hasNext()) {
      val t = tIter.next()
      t.update(dt)
      if (t.isDead) tIter.remove()
    }

    // Update shake
    if (shakeDuration > 0f) {
      shakeDuration -= dt
      val mag = shakeIntensity * (shakeDuration / 0.3f).coerceAtLeast(0f)
      shakeOffset = Offset(
        (Random.nextFloat() * 2f - 1f) * mag,
        (Random.nextFloat() * 2f - 1f) * mag
      )
    } else {
      shakeOffset = Offset.Zero
    }
  }
}
