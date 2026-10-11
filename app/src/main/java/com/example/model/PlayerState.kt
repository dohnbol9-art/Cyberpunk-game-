package com.example.model

data class PlayerState(
  val hullHp: Int = 100,
  val maxHullHp: Int = 100,
  val shield: Int = 0,
  val energy: Int = 30,
  val maxEnergy: Int = 100,
  val movesRemaining: Int = 20,
  val credits: Int = 60,
  val score: Int = 0,
  val neonShards: Int = 0,
  val relics: List<Relic> = emptyList(),
  val abilities: List<CyberAbility> = emptyList(),
  val runSector: Int = 1,
  val maxCombosAchieved: Int = 0,
  val totalDamageDealt: Int = 0,
  val hasCritBoost: Boolean = false,
  val damageMultiplier: Float = 1.0f
) {
  val hpPercent: Float get() = (hullHp.toFloat() / maxHullHp.coerceAtLeast(1)).coerceIn(0f, 1f)
  val energyPercent: Float get() = (energy.toFloat() / maxEnergy.coerceAtLeast(1)).coerceIn(0f, 1f)
  val isAlive: Boolean get() = hullHp > 0
}

data class MatchGroup(
  val gems: List<Gem>,
  val type: GemType,
  val isHorizontal: Boolean,
  val isVertical: Boolean,
  val length: Int
)

data class StepCascadeResult(
  val matchedCoords: Set<Pair<Int, Int>>,
  val createdSpecials: Map<Pair<Int, Int>, Pair<GemType, SpecialType>>,
  val baseScore: Int,
  val damageDealt: Int,
  val shieldGained: Int,
  val healGained: Int,
  val energyGained: Int,
  val isCritical: Boolean,
  val comboStep: Int
)
