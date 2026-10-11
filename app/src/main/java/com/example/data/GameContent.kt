package com.example.data

import com.example.model.*

object GameContent {

  val ALL_RELICS = listOf(
    Relic(
      id = "relic_overclock_battery",
      name = "Overclock Capacitor",
      description = "Chain combos of 2+ restore +1 extra move.",
      rarity = RelicRarity.RARE,
      costCredits = 70,
      iconName = "battery"
    ),
    Relic(
      id = "relic_prism_splitter",
      name = "Prism Dual-Splitter",
      description = "4-gem matches trigger both row and column laser sweeps.",
      rarity = RelicRarity.LEGENDARY,
      costCredits = 100,
      iconName = "prism"
    ),
    Relic(
      id = "relic_nanite_regen",
      name = "Bio-Nanite Infusion",
      description = "Acid Emerald (Bio) matches heal Hull for 3 HP per gem.",
      rarity = RelicRarity.COMMON,
      costCredits = 45,
      iconName = "nanite"
    ),
    Relic(
      id = "relic_plasma_dynamo",
      name = "Void Dynamo",
      description = "Violet Orb matches award double Cyber Energy.",
      rarity = RelicRarity.COMMON,
      costCredits = 40,
      iconName = "dynamo"
    ),
    Relic(
      id = "relic_static_nova",
      name = "Static Discharge",
      description = "Creating a 5-match Hyper Nova zaps 4 random enemies/tiles.",
      rarity = RelicRarity.RARE,
      costCredits = 75,
      iconName = "static"
    ),
    Relic(
      id = "relic_aegis_matrix",
      name = "Cryo Aegis Matrix",
      description = "Cyan Cryo matches generate +4 Cyber Shield.",
      rarity = RelicRarity.COMMON,
      costCredits = 50,
      iconName = "shield"
    ),
    Relic(
      id = "relic_solar_overload",
      name = "Solar Supercharger",
      description = "Amber Solar matches deal +60% additional damage.",
      rarity = RelicRarity.RARE,
      costCredits = 65,
      iconName = "solar"
    ),
    Relic(
      id = "relic_crit_protocol",
      name = "Critical Algorithm",
      description = "25% chance for any match to deal 2.5x Critical Overload.",
      rarity = RelicRarity.RARE,
      costCredits = 70,
      iconName = "crit"
    ),
    Relic(
      id = "relic_glitch_recycler",
      name = "Glitch Purger",
      description = "Purging corrupted nodes restores 12 HP and gives 15 Energy.",
      rarity = RelicRarity.COMMON,
      costCredits = 45,
      iconName = "glitch"
    ),
    Relic(
      id = "relic_credit_sniffer",
      name = "Bit Synthesizer",
      description = "Earn +50% Cyber Credits after every battle.",
      rarity = RelicRarity.COMMON,
      costCredits = 40,
      iconName = "credits"
    ),
    Relic(
      id = "relic_chrono_warp",
      name = "Temporal Core",
      description = "All combat encounters start with +4 additional moves.",
      rarity = RelicRarity.LEGENDARY,
      costCredits = 95,
      iconName = "chrono"
    ),
    Relic(
      id = "relic_reactive_plating",
      name = "Reactive Plating",
      description = "When taking damage, instantly detonate 2 random board tiles.",
      rarity = RelicRarity.RARE,
      costCredits = 60,
      iconName = "plating"
    )
  )

  val ALL_ABILITIES = listOf(
    CyberAbility(
      type = AbilityType.EMP_PULSE,
      name = "EMP Surge",
      description = "Clears all Corrupted tiles and shuffles the grid.",
      energyCost = 25,
      iconName = "emp"
    ),
    CyberAbility(
      type = AbilityType.ORBITAL_LASER,
      name = "Ion Laser",
      description = "Target any cell to unleash a full crossbeam laser explosion.",
      energyCost = 35,
      iconName = "laser"
    ),
    CyberAbility(
      type = AbilityType.QUANTUM_SWAP,
      name = "Quantum Swap",
      description = "Free swap any two tiles regardless of adjacency.",
      energyCost = 20,
      iconName = "swap"
    ),
    CyberAbility(
      type = AbilityType.OVERCLOCK_TURN,
      name = "Overclock Hack",
      description = "Instantly gain +3 moves and boost next combo damage by 50%.",
      energyCost = 40,
      iconName = "overclock"
    ),
    CyberAbility(
      type = AbilityType.NOVA_SYNTHESIS,
      name = "Nova Synthesis",
      description = "Synthesizes a rainbow Hyper Nova at selected position.",
      energyCost = 45,
      iconName = "nova"
    )
  )

  fun getCommonEnemies(depth: Int): List<Enemy> {
    val scale = 1.0f + depth * 0.18f
    return listOf(
      Enemy(
        id = "e_sentinel",
        name = "Glitch Sentinel",
        title = "Automated Security Drone",
        maxHp = (180 * scale).toInt(),
        currentHp = (180 * scale).toInt(),
        attackDamage = (14 * scale).toInt(),
        maxCooldown = 3,
        currentCooldown = 3,
        passiveDescription = "Attacks every 3 moves. High kinetic impact.",
        avatarSymbol = "DRONE"
      ),
      Enemy(
        id = "e_spider",
        name = "Cyber Arachnid",
        title = "Sub-routine Webweaver",
        maxHp = (150 * scale).toInt(),
        currentHp = (150 * scale).toInt(),
        attackDamage = (10 * scale).toInt(),
        maxCooldown = 2,
        currentCooldown = 2,
        passiveDescription = "Fast strikes every 2 moves. Corrupts 1 random tile.",
        avatarSymbol = "ARACHNID"
      ),
      Enemy(
        id = "e_phantom",
        name = "Data Phantom",
        title = "Rogue AI Remnant",
        maxHp = (210 * scale).toInt(),
        currentHp = (210 * scale).toInt(),
        attackDamage = (18 * scale).toInt(),
        maxCooldown = 4,
        currentCooldown = 4,
        passiveDescription = "Charges a powerful byte strike every 4 moves.",
        avatarSymbol = "PHANTOM"
      )
    )
  }

  fun getEliteEnemies(depth: Int): List<Enemy> {
    val scale = 1.25f + depth * 0.22f
    return listOf(
      Enemy(
        id = "e_vanguard",
        name = "Quantum Colossus",
        title = "Elite Firewall Guardian",
        maxHp = (360 * scale).toInt(),
        currentHp = (360 * scale).toInt(),
        attackDamage = (24 * scale).toInt(),
        maxCooldown = 3,
        currentCooldown = 3,
        passiveDescription = "Spawns 2 Corrupted nodes upon attacking.",
        isElite = true,
        avatarSymbol = "COLOSSUS"
      ),
      Enemy(
        id = "e_hacker",
        name = "Cipher Executioner",
        title = "Neural Overwrite Protocol",
        maxHp = (320 * scale).toInt(),
        currentHp = (320 * scale).toInt(),
        attackDamage = (28 * scale).toInt(),
        maxCooldown = 4,
        currentCooldown = 4,
        passiveDescription = "Massive strike. Drains 15 player energy on hit.",
        isElite = true,
        avatarSymbol = "CIPHER"
      )
    )
  }

  fun getBossEnemy(sector: Int): Enemy {
    val scale = 1.0f + (sector - 1) * 0.4f
    return Enemy(
      id = "e_boss_archon",
      name = "ARCHON OVERMIND v${sector}.0",
      title = "Sector Core Sentience",
      maxHp = (650 * scale).toInt(),
      currentHp = (650 * scale).toInt(),
      attackDamage = (35 * scale).toInt(),
      maxCooldown = 3,
      currentCooldown = 3,
      passiveDescription = "Apex Overseer. Corrupts 2 nodes and strikes for heavy damage.",
      isBoss = true,
      avatarSymbol = "OVERMIND"
    )
  }

  data class MysteryEvent(
    val title: String,
    val description: String,
    val optionA: EventOption,
    val optionB: EventOption
  )

  data class EventOption(
    val title: String,
    val description: String,
    val outcomeType: EventOutcome
  )

  enum class EventOutcome {
    HEAL_FULL,
    SACRIFICE_HP_FOR_RELIC,
    GAIN_CREDITS,
    GAIN_ENERGY_AND_SHIELD,
    GAIN_SHARDS
  }

  val MYSTERY_EVENTS = listOf(
    MysteryEvent(
      title = "Corrupted Nanite Cache",
      description = "You discover an abandoned security lockbox emitting pulsing violet radiation. Do you crack the encryption or recycle the scrap?",
      optionA = EventOption(
        title = "Force Override (-18 HP)",
        description = "Sustain electrical feedback to extract an experimental Cyber Relic.",
        outcomeType = EventOutcome.SACRIFICE_HP_FOR_RELIC
      ),
      optionB = EventOption(
        title = "Scrap Salvage",
        description = "Safely disassemble the chassis for 60 Cyber Credits.",
        outcomeType = EventOutcome.GAIN_CREDITS
      )
    ),
    MysteryEvent(
      title = "Overclock Charging Pylon",
      description = "A humming power pylon pulses with pure neon plasma. Your system core detects an opportunity to overclock.",
      optionA = EventOption(
        title = "Siphon Power",
        description = "Charge +40 Energy and deploy +30 Shield.",
        outcomeType = EventOutcome.GAIN_ENERGY_AND_SHIELD
      ),
      optionB = EventOption(
        title = "Extract Neon Shards",
        description = "Harvest 25 persistent Neon Shards for Citadel meta-upgrades.",
        outcomeType = EventOutcome.GAIN_SHARDS
      )
    ),
    MysteryEvent(
      title = "Rogue Med-Bay",
      description = "An unattended clinical subroutine offers rapid neural repair protocols.",
      optionA = EventOption(
        title = "Full Nanite Repair",
        description = "Restore Hull HP to 100%.",
        outcomeType = EventOutcome.HEAL_FULL
      ),
      optionB = EventOption(
        title = "Convert Nanites to Currency",
        description = "Drain medical tanks for 75 Cyber Credits.",
        outcomeType = EventOutcome.GAIN_CREDITS
      )
    )
  )

  data class MetaUpgrade(
    val id: String,
    val name: String,
    val description: String,
    val maxLevel: Int,
    val baseCostShards: Int,
    val currentLevel: Int = 0
  ) {
    val nextCost: Int get() = baseCostShards * (currentLevel + 1)
    val isMaxed: Boolean get() = currentLevel >= maxLevel
  }

  val DEFAULT_META_UPGRADES = listOf(
    MetaUpgrade("meta_hull", "Reinforced Chassis", "+12 Max Hull HP per rank", maxLevel = 5, baseCostShards = 20),
    MetaUpgrade("meta_energy", "Capacitor Expansion", "+15 Max & Starting Energy per rank", maxLevel = 4, baseCostShards = 25),
    MetaUpgrade("meta_moves", "Tactical Buffer", "+1 extra starting move per combat rank", maxLevel = 3, baseCostShards = 40),
    MetaUpgrade("meta_credits", "Data Mining Subroutines", "Start each run with +25 bonus credits per rank", maxLevel = 4, baseCostShards = 20),
    MetaUpgrade("meta_crit", "Precision Targeting", "+5% base critical overload strike chance", maxLevel = 4, baseCostShards = 30)
  )

  data class HeroClass(
    val id: String,
    val name: String,
    val title: String,
    val description: String,
    val startingHp: Int,
    val startingEnergy: Int,
    val startingAbility: CyberAbility,
    val startingRelic: Relic,
    val accentColorHex: Long
  )

  val HERO_CLASSES = listOf(
    HeroClass(
      id = "class_striker",
      name = "Neon Striker",
      title = "High-Voltage Vanguard",
      description = "Aggressive protocol. Starts with Ion Laser strike and Solar Supercharger for massive burst damage.",
      startingHp = 100,
      startingEnergy = 35,
      startingAbility = ALL_ABILITIES[1], // ORBITAL_LASER
      startingRelic = ALL_RELICS[6],      // Solar Supercharger
      accentColorHex = 0xFFFF007FL
    ),
    HeroClass(
      id = "class_chrono",
      name = "Chrono Weaver",
      title = "Temporal Hacker",
      description = "Manipulates turns and board states. Starts with Overclock Hack and Overclock Capacitor for endless moves.",
      startingHp = 90,
      startingEnergy = 45,
      startingAbility = ALL_ABILITIES[3], // OVERCLOCK_TURN
      startingRelic = ALL_RELICS[0],      // Overclock Capacitor
      accentColorHex = 0xFF00F5FFL
    ),
    HeroClass(
      id = "class_prism",
      name = "Prism Synthesizer",
      title = "Matrix Alchemist",
      description = "Master of special tile combinations. Starts with Nova Synthesis and Prism Dual-Splitter.",
      startingHp = 110,
      startingEnergy = 40,
      startingAbility = ALL_ABILITIES[4], // NOVA_SYNTHESIS
      startingRelic = ALL_RELICS[1],      // Prism Splitter
      accentColorHex = 0xFF39FF14L
    )
  )
}
