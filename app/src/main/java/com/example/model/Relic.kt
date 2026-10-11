package com.example.model

enum class RelicRarity(val label: String) {
  COMMON("Common"),
  RARE("Rare"),
  LEGENDARY("Legendary")
}

data class Relic(
  val id: String,
  val name: String,
  val description: String,
  val rarity: RelicRarity,
  val costCredits: Int = 50,
  val iconName: String = "battery_charging_full"
)

enum class AbilityType {
  EMP_PULSE,
  ORBITAL_LASER,
  QUANTUM_SWAP,
  OVERCLOCK_TURN,
  NOVA_SYNTHESIS
}

data class CyberAbility(
  val type: AbilityType,
  val name: String,
  val description: String,
  val energyCost: Int,
  val iconName: String
)
