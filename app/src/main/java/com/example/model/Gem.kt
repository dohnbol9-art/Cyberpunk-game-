package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class GemType(
  val displayName: String,
  val color: Color,
  val glowColor: Color,
  val elementName: String
) {
  CYAN_PRISM("Cryo Prism", NeonCyan, Color(0x6600F5FF), "Cryo"),
  MAGENTA_HEART("Pulse Core", NeonMagenta, Color(0x66FF007F), "Pulse"),
  AMBER_STAR("Solar Star", NeonAmber, Color(0x66FFE600), "Solar"),
  ACID_EMERALD("Bio Nanite", NeonGreen, Color(0x6639FF14), "Bio"),
  VIOLET_ORB("Void Orb", NeonPurple, Color(0x66BD00FF), "Void"),
  ORANGE_FLAME("Pyro Spark", NeonOrange, Color(0x66FF6A00), "Pyro")
}

enum class SpecialType(val label: String) {
  NONE("Standard"),
  BEAM_HORIZONTAL("Row Laser"),
  BEAM_VERTICAL("Column Laser"),
  NEON_BOMB("Neon Blast"),
  COLOR_NOVA("Hyper Nova"),
  CORRUPTED("Glitch Node")
}

data class Gem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val type: GemType,
  val special: SpecialType = SpecialType.NONE,
  val row: Int,
  val col: Int,
  val isMatched: Boolean = false,
  val isHighlighted: Boolean = false
) {
  fun copyWithPosition(r: Int, c: Int): Gem = copy(row = r, col = c)
}
