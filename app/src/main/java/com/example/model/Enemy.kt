package com.example.model

data class Enemy(
  val id: String,
  val name: String,
  val title: String,
  val maxHp: Int,
  val currentHp: Int,
  val attackDamage: Int,
  val maxCooldown: Int,
  val currentCooldown: Int,
  val passiveDescription: String,
  val isBoss: Boolean = false,
  val isElite: Boolean = false,
  val avatarSymbol: String = "TERMINAL"
) {
  val hpPercent: Float get() = (currentHp.toFloat() / maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
  val isDefeated: Boolean get() = currentHp <= 0
}

enum class NodeType(val label: String) {
  BATTLE("Cyber Skirmish"),
  ELITE("Elite Sentinel"),
  REST("Nanite Chamber"),
  SHOP("Black Market"),
  EVENT("Encrypted Cache"),
  BOSS("Core Overseer")
}

data class MapNode(
  val id: String,
  val depth: Int, // 0 to 7
  val lane: Int,  // 0, 1, 2
  val type: NodeType,
  val isCleared: Boolean = false,
  val isCurrent: Boolean = false,
  val isAvailable: Boolean = false,
  val nextNodeIds: List<String> = emptyList()
)

data class SectorMap(
  val nodes: List<MapNode>,
  val currentDepth: Int = 0
)
