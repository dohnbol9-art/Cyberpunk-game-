package com.example.viewmodel

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameContent
import com.example.data.MetaProgressionRepository
import com.example.engine.*
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameScreen {
  MAIN_MENU,
  SECTOR_MAP,
  BATTLE,
  SHOP,
  EVENT,
  REST,
  CITADEL,
  VICTORY,
  GAME_OVER
}

enum class TargetingAction {
  NONE,
  ORBITAL_LASER,
  QUANTUM_SWAP_FIRST,
  QUANTUM_SWAP_SECOND,
  NOVA_SYNTHESIS
}

data class ShopItem(
  val relic: Relic,
  val isPurchased: Boolean = false
)

data class GameUiState(
  val currentScreen: GameScreen = GameScreen.MAIN_MENU,
  val selectedHeroClass: GameContent.HeroClass = GameContent.HERO_CLASSES.first(),
  val player: PlayerState = PlayerState(),
  val sectorMap: SectorMap = SectorMap(emptyList()),
  val currentEnemy: Enemy? = null,
  val board: Array<Array<Gem>> = Match3Engine.createInitialBoard(),
  val selectedGemCoord: Pair<Int, Int>? = null,
  val isCascading: Boolean = false,
  val targetingAction: TargetingAction = TargetingAction.NONE,
  val quantumFirstCoord: Pair<Int, Int>? = null,
  val statusMessage: String = "Cyber Matrix Online",
  val currentEvent: GameContent.MysteryEvent? = null,
  val shopRelics: List<ShopItem> = emptyList(),
  val metaUpgrades: List<GameContent.MetaUpgrade> = GameContent.DEFAULT_META_UPGRADES,
  val totalNeonShards: Int = 0,
  val highScore: Int = 0,
  val currentCombo: Int = 0,
  val lastCombatRewardCredits: Int = 0,
  val lastCombatRewardShards: Int = 0,
  val victoryMessage: String = ""
)

class GameViewModel(private val context: Context) : ViewModel() {
  private val metaRepo = MetaProgressionRepository(context)
  val particleManager = ParticleManager()

  private val _uiState = MutableStateFlow(
    GameUiState(
      metaUpgrades = loadMetaUpgrades(),
      totalNeonShards = metaRepo.getNeonShards(),
      highScore = metaRepo.getHighScore()
    )
  )
  val uiState = _uiState.asStateFlow()

  private fun loadMetaUpgrades(): List<GameContent.MetaUpgrade> {
    return GameContent.DEFAULT_META_UPGRADES.map { up ->
      val lvl = metaRepo.getUpgradeLevel(up.id)
      up.copy(currentLevel = lvl)
    }
  }

  fun navigateTo(screen: GameScreen) {
    _uiState.update { it.copy(currentScreen = screen) }
  }

  fun selectHeroClass(heroClass: GameContent.HeroClass) {
    _uiState.update { it.copy(selectedHeroClass = heroClass) }
  }

  fun startNewRun() {
    val hero = _uiState.value.selectedHeroClass
    val upgrades = _uiState.value.metaUpgrades

    val hullLvl = upgrades.find { it.id == "meta_hull" }?.currentLevel ?: 0
    val energyLvl = upgrades.find { it.id == "meta_energy" }?.currentLevel ?: 0
    val movesLvl = upgrades.find { it.id == "meta_moves" }?.currentLevel ?: 0
    val creditsLvl = upgrades.find { it.id == "meta_credits" }?.currentLevel ?: 0
    val critLvl = upgrades.find { it.id == "meta_crit" }?.currentLevel ?: 0

    val maxHp = hero.startingHp + hullLvl * 12
    val maxEnergy = hero.startingEnergy + energyLvl * 15
    val startingMoves = 22 + movesLvl
    val startingCredits = 50 + creditsLvl * 25

    val generatedMap = SectorMapGenerator.generateSector(1)

    val player = PlayerState(
      hullHp = maxHp,
      maxHullHp = maxHp,
      shield = 10,
      energy = maxEnergy,
      maxEnergy = maxEnergy,
      movesRemaining = startingMoves,
      credits = startingCredits,
      score = 0,
      neonShards = 0,
      relics = listOf(hero.startingRelic),
      abilities = listOf(hero.startingAbility, GameContent.ALL_ABILITIES[0]), // Starting + EMP Surge
      runSector = 1,
      hasCritBoost = critLvl > 0
    )

    _uiState.update {
      it.copy(
        player = player,
        sectorMap = generatedMap,
        currentScreen = GameScreen.SECTOR_MAP,
        board = Match3Engine.createInitialBoard(),
        selectedGemCoord = null,
        isCascading = false,
        targetingAction = TargetingAction.NONE,
        statusMessage = "Sector 1 Matrix Initialized. Select starting node."
      )
    }
  }

  fun onSelectMapNode(node: MapNode) {
    if (!node.isAvailable && !node.isCurrent) return

    val updatedNodes = _uiState.value.sectorMap.nodes.map {
      if (it.id == node.id) it.copy(isCurrent = true, isAvailable = false)
      else if (it.isCurrent) it.copy(isCurrent = false, isCleared = true)
      else it
    }

    _uiState.update {
      it.copy(
        sectorMap = it.sectorMap.copy(nodes = updatedNodes, currentDepth = node.depth)
      )
    }

    when (node.type) {
      NodeType.BATTLE -> startCombat(isElite = false, isBoss = false, depth = node.depth)
      NodeType.ELITE -> startCombat(isElite = true, isBoss = false, depth = node.depth)
      NodeType.BOSS -> startCombat(isElite = false, isBoss = true, depth = node.depth)
      NodeType.SHOP -> openShop()
      NodeType.EVENT -> openEvent()
      NodeType.REST -> openRest()
    }
  }

  private fun startCombat(isElite: Boolean, isBoss: Boolean, depth: Int) {
    val enemy = when {
      isBoss -> GameContent.getBossEnemy(_uiState.value.player.runSector)
      isElite -> GameContent.getEliteEnemies(depth).random()
      else -> GameContent.getCommonEnemies(depth).random()
    }

    // Apply temporal core relic bonus if equipped
    val hasTemporalRelic = _uiState.value.player.relics.any { it.id == "relic_chrono_warp" }
    val bonusMoves = if (hasTemporalRelic) 4 else 0
    val combatMoves = 22 + bonusMoves

    val initialBoard = Match3Engine.createInitialBoard()

    _uiState.update {
      it.copy(
        currentEnemy = enemy,
        board = initialBoard,
        selectedGemCoord = null,
        isCascading = false,
        targetingAction = TargetingAction.NONE,
        player = it.player.copy(movesRemaining = combatMoves, shield = (it.player.shield + 5).coerceAtMost(50)),
        currentScreen = GameScreen.BATTLE,
        statusMessage = "ENGAGING: ${enemy.name}"
      )
    }
    vibrate(50)
  }

  private fun openShop() {
    val availableRelics = GameContent.ALL_RELICS
      .filter { relic -> _uiState.value.player.relics.none { it.id == relic.id } }
      .shuffled()
      .take(4)
      .map { ShopItem(it) }

    _uiState.update {
      it.copy(
        shopRelics = availableRelics,
        currentScreen = GameScreen.SHOP,
        statusMessage = "Black Market Terminal Connected"
      )
    }
  }

  fun buyShopRelic(shopItem: ShopItem) {
    val p = _uiState.value.player
    if (p.credits >= shopItem.relic.costCredits && !shopItem.isPurchased) {
      val updatedPlayer = p.copy(
        credits = p.credits - shopItem.relic.costCredits,
        relics = p.relics + shopItem.relic
      )
      val updatedShop = _uiState.value.shopRelics.map {
        if (it.relic.id == shopItem.relic.id) it.copy(isPurchased = true) else it
      }
      _uiState.update {
        it.copy(
          player = updatedPlayer,
          shopRelics = updatedShop,
          statusMessage = "Acquired: ${shopItem.relic.name}!"
        )
      }
      vibrate(40)
    }
  }

  fun buyShopHeal() {
    val p = _uiState.value.player
    val cost = 30
    if (p.credits >= cost && p.hullHp < p.maxHullHp) {
      val healed = (p.hullHp + 35).coerceAtMost(p.maxHullHp)
      _uiState.update {
        it.copy(
          player = p.copy(credits = p.credits - cost, hullHp = healed),
          statusMessage = "Hull Repaired (+35 HP)"
        )
      }
      vibrate(30)
    }
  }

  private fun openEvent() {
    val event = GameContent.MYSTERY_EVENTS.random()
    _uiState.update {
      it.copy(
        currentEvent = event,
        currentScreen = GameScreen.EVENT,
        statusMessage = "Unidentified Signal Detected"
      )
    }
  }

  fun resolveEventOption(option: GameContent.EventOption) {
    val p = _uiState.value.player
    when (option.outcomeType) {
      GameContent.EventOutcome.HEAL_FULL -> {
        _uiState.update {
          it.copy(
            player = p.copy(hullHp = p.maxHullHp),
            statusMessage = "Nanite Restorative Complete! HP 100%"
          )
        }
      }
      GameContent.EventOutcome.SACRIFICE_HP_FOR_RELIC -> {
        val newRelic = GameContent.ALL_RELICS.filter { relic -> p.relics.none { it.id == relic.id } }.randomOrNull()
        val dmg = 18
        val newHp = (p.hullHp - dmg).coerceAtLeast(1)
        val relics = if (newRelic != null) p.relics + newRelic else p.relics
        _uiState.update {
          it.copy(
            player = p.copy(hullHp = newHp, relics = relics),
            statusMessage = "Extracted ${newRelic?.name ?: "Relic"}! Sustained $dmg damage."
          )
        }
      }
      GameContent.EventOutcome.GAIN_CREDITS -> {
        _uiState.update {
          it.copy(
            player = p.copy(credits = p.credits + 65),
            statusMessage = "Salvage Successful (+65 Credits)"
          )
        }
      }
      GameContent.EventOutcome.GAIN_ENERGY_AND_SHIELD -> {
        _uiState.update {
          it.copy(
            player = p.copy(
              energy = (p.energy + 40).coerceAtMost(p.maxEnergy),
              shield = (p.shield + 30).coerceAtMost(60)
            ),
            statusMessage = "Overcharged: +40 Energy, +30 Shield"
          )
        }
      }
      GameContent.EventOutcome.GAIN_SHARDS -> {
        metaRepo.addNeonShards(25)
        _uiState.update {
          it.copy(
            totalNeonShards = metaRepo.getNeonShards(),
            statusMessage = "Harvested 25 Neon Shards!"
          )
        }
      }
    }
    finishNonCombatNode()
  }

  private fun openRest() {
    _uiState.update {
      it.copy(
        currentScreen = GameScreen.REST,
        statusMessage = "Nanite Regeneration Chamber"
      )
    }
  }

  fun restHeal() {
    val p = _uiState.value.player
    val healAmount = (p.maxHullHp * 0.4f).toInt()
    val newHp = (p.hullHp + healAmount).coerceAtMost(p.maxHullHp)
    _uiState.update {
      it.copy(
        player = p.copy(hullHp = newHp),
        statusMessage = "Restored $healAmount Hull HP"
      )
    }
    finishNonCombatNode()
  }

  fun restOverclock() {
    val p = _uiState.value.player
    _uiState.update {
      it.copy(
        player = p.copy(
          damageMultiplier = p.damageMultiplier + 0.25f,
          energy = p.maxEnergy
        ),
        statusMessage = "Core Overclocked: +25% Damage, Max Energy"
      )
    }
    finishNonCombatNode()
  }

  fun finishNonCombatNode() {
    advanceSectorMap()
    _uiState.update { it.copy(currentScreen = GameScreen.SECTOR_MAP) }
  }

  private fun advanceSectorMap() {
    val currentMap = _uiState.value.sectorMap
    val currentNode = currentMap.nodes.find { it.isCurrent }
    val nextIds = currentNode?.nextNodeIds ?: emptyList()

    val updatedNodes = currentMap.nodes.map { node ->
      if (node.id == currentNode?.id) {
        node.copy(isCurrent = false, isCleared = true)
      } else if (node.id in nextIds) {
        node.copy(isAvailable = true)
      } else {
        node
      }
    }

    _uiState.update {
      it.copy(
        sectorMap = currentMap.copy(nodes = updatedNodes, currentDepth = (currentNode?.depth ?: 0) + 1)
      )
    }
  }

  /**
   * Gem Tap Handler
   */
  fun onGemClick(r: Int, c: Int, cellSizePx: Float, boardOrigin: Offset) {
    if (_uiState.value.isCascading) return

    val state = _uiState.value

    // Handle Active Abilities targeting
    when (state.targetingAction) {
      TargetingAction.ORBITAL_LASER -> {
        applyOrbitalLaser(r, c, cellSizePx, boardOrigin)
        return
      }
      TargetingAction.QUANTUM_SWAP_FIRST -> {
        _uiState.update {
          it.copy(
            quantumFirstCoord = Pair(r, c),
            targetingAction = TargetingAction.QUANTUM_SWAP_SECOND,
            statusMessage = "Select second gem to swap."
          )
        }
        return
      }
      TargetingAction.QUANTUM_SWAP_SECOND -> {
        val first = state.quantumFirstCoord
        if (first != null && (first.first != r || first.second != c)) {
          executeSwap(first.first, first.second, r, c, cellSizePx, boardOrigin, isFreeSwap = true)
        }
        _uiState.update { it.copy(targetingAction = TargetingAction.NONE, quantumFirstCoord = null) }
        return
      }
      TargetingAction.NOVA_SYNTHESIS -> {
        applyNovaSynthesis(r, c, cellSizePx, boardOrigin)
        return
      }
      else -> Unit
    }

    val selected = state.selectedGemCoord
    if (selected == null) {
      _uiState.update { it.copy(selectedGemCoord = Pair(r, c)) }
      vibrate(20)
    } else {
      val (sr, sc) = selected
      if (sr == r && sc == c) {
        // Deselect
        _uiState.update { it.copy(selectedGemCoord = null) }
      } else if (Match3Engine.areAdjacent(sr, sc, r, c)) {
        _uiState.update { it.copy(selectedGemCoord = null) }
        executeSwap(sr, sc, r, c, cellSizePx, boardOrigin, isFreeSwap = false)
      } else {
        // Select new gem
        _uiState.update { it.copy(selectedGemCoord = Pair(r, c)) }
        vibrate(20)
      }
    }
  }

  private fun executeSwap(
    r1: Int, c1: Int,
    r2: Int, c2: Int,
    cellSizePx: Float,
    boardOrigin: Offset,
    isFreeSwap: Boolean
  ) {
    val currentBoard = _uiState.value.board
    val gem1 = currentBoard[r1][c1]
    val gem2 = currentBoard[r2][c2]

    val isNovaSwap = gem1.special == SpecialType.COLOR_NOVA || gem2.special == SpecialType.COLOR_NOVA
    val canMatch = Match3Engine.canSwapMakeMatch(currentBoard, r1, c1, r2, c2)

    if (!isFreeSwap && !isNovaSwap && !canMatch) {
      // Invalid swap
      particleManager.triggerShake(intensity = 6f, duration = 0.2f)
      _uiState.update { it.copy(statusMessage = "No match detected. Reverting swap.") }
      vibrate(40)
      return
    }

    // Valid swap: swap gems
    val swappedBoard = Array(Match3Engine.ROWS) { r ->
      Array(Match3Engine.COLS) { c ->
        currentBoard[r][c].copy()
      }
    }
    swappedBoard[r1][c1] = gem2.copyWithPosition(r1, c1)
    swappedBoard[r2][c2] = gem1.copyWithPosition(r2, c2)

    val currentMoves = _uiState.value.player.movesRemaining
    val newMoves = if (isFreeSwap) currentMoves else (currentMoves - 1).coerceAtLeast(0)

    _uiState.update {
      it.copy(
        board = swappedBoard,
        player = it.player.copy(movesRemaining = newMoves),
        isCascading = true,
        currentCombo = 0
      )
    }
    vibrate(30)

    // Run Cascading Loop
    viewModelScope.launch {
      runCascadeLoop(
        initialSwappedCoords = Pair(Pair(r1, c1), Pair(r2, c2)),
        cellSizePx = cellSizePx,
        boardOrigin = boardOrigin
      )
    }
  }

  private suspend fun runCascadeLoop(
    initialSwappedCoords: Pair<Pair<Int, Int>, Pair<Int, Int>>?,
    cellSizePx: Float,
    boardOrigin: Offset
  ) {
    var board = _uiState.value.board
    var combo = 1
    var lastSwapped = initialSwappedCoords

    while (true) {
      val result = Match3Engine.evaluateBoardStep(
        board = board,
        lastSwappedCoords = lastSwapped,
        relics = _uiState.value.player.relics,
        comboIndex = combo,
        damageMultiplier = _uiState.value.player.damageMultiplier
      )

      if (result.matchedCoords.isEmpty()) {
        break
      }

      // Spawn visual particle explosions
      for (coord in result.matchedCoords) {
        val center = Offset(
          boardOrigin.x + coord.second * cellSizePx + cellSizePx / 2f,
          boardOrigin.y + coord.first * cellSizePx + cellSizePx / 2f
        )
        val gemType = board[coord.first][coord.second].type
        particleManager.spawnMatchBurst(center.x, center.y, gemType.color, count = 12)

        val spec = board[coord.first][coord.second].special
        if (spec == SpecialType.NEON_BOMB) {
          particleManager.spawnShockwave(center, NeonMagenta)
        } else if (spec == SpecialType.BEAM_HORIZONTAL) {
          particleManager.spawnLaser(
            Offset(boardOrigin.x, center.y),
            Offset(boardOrigin.x + Match3Engine.COLS * cellSizePx, center.y),
            gemType.color,
            isHorizontal = true
          )
        } else if (spec == SpecialType.BEAM_VERTICAL) {
          particleManager.spawnLaser(
            Offset(center.x, boardOrigin.y),
            Offset(center.x, boardOrigin.y + Match3Engine.ROWS * cellSizePx),
            gemType.color,
            isHorizontal = false
          )
        }
      }

      // Spawn floating damage & combo numbers
      val boardCenterX = boardOrigin.x + (Match3Engine.COLS * cellSizePx) / 2f
      val boardCenterY = boardOrigin.y + (Match3Engine.ROWS * cellSizePx) / 2f
      val dmgText = if (result.isCritical) "CRIT OVERLOAD! -${result.damageDealt}" else "-${result.damageDealt} DMG"
      particleManager.spawnFloatingText(
        x = boardCenterX + (Random.nextFloat() * 60f - 30f),
        y = boardCenterY - 40f,
        text = dmgText,
        color = if (result.isCritical) NeonAmber else NeonCyan,
        isCrit = result.isCritical
      )

      if (combo >= 2) {
        particleManager.spawnFloatingText(
          x = boardCenterX,
          y = boardCenterY - 90f,
          text = "COMBO x$combo!",
          color = NeonMagenta
        )
      }

      particleManager.triggerShake(intensity = if (result.isCritical) 14f else 8f, duration = 0.25f)
      vibrate(if (result.isCritical) 60 else 30)

      // Apply damage to enemy
      val curEnemy = _uiState.value.currentEnemy
      val newEnemyHp = if (curEnemy != null) (curEnemy.currentHp - result.damageDealt).coerceAtLeast(0) else 0

      // Relic Overclock Battery: combo >= 2 restores +1 move
      val hasOverclockBattery = _uiState.value.player.relics.any { it.id == "relic_overclock_battery" }
      val bonusMove = if (hasOverclockBattery && combo >= 2) 1 else 0
      if (bonusMove > 0) {
        particleManager.spawnFloatingText(boardCenterX, boardCenterY - 130f, "+1 MOVE!", NeonGreen)
      }

      val p = _uiState.value.player
      val updatedPlayer = p.copy(
        score = p.score + result.baseScore,
        energy = (p.energy + result.energyGained).coerceAtMost(p.maxEnergy),
        shield = (p.shield + result.shieldGained).coerceAtMost(70),
        hullHp = (p.hullHp + result.healGained).coerceAtMost(p.maxHullHp),
        movesRemaining = p.movesRemaining + bonusMove,
        totalDamageDealt = p.totalDamageDealt + result.damageDealt,
        maxCombosAchieved = maxOf(p.maxCombosAchieved, combo)
      )

      // Apply Gravity and Cascade
      board = Match3Engine.applyCascade(board, result.matchedCoords, result.createdSpecials)

      _uiState.update {
        it.copy(
          board = board,
          player = updatedPlayer,
          currentEnemy = curEnemy?.copy(currentHp = newEnemyHp),
          currentCombo = combo,
          statusMessage = "Combo x$combo: ${result.damageDealt} Damage Dealt!"
        )
      }

      delay(320)
      combo++
      lastSwapped = null // Only active for first step

      // Check if enemy defeated mid-cascade
      if (newEnemyHp <= 0) {
        break
      }
    }

    _uiState.update { it.copy(isCascading = false) }

    // Check combat resolution
    checkCombatPostTurn()
  }

  private fun checkCombatPostTurn() {
    val enemy = _uiState.value.currentEnemy ?: return
    val p = _uiState.value.player

    if (enemy.isDefeated) {
      handleCombatVictory(enemy)
      return
    }

    if (p.movesRemaining <= 0) {
      // Out of moves
      handleCombatDefeat("Tactical moves exhausted! Core shutdown.")
      return
    }

    // Enemy Turn Countdown
    val newCooldown = enemy.currentCooldown - 1
    if (newCooldown <= 0) {
      // Enemy Strikes!
      val rawDmg = enemy.attackDamage
      val currentShield = p.shield
      val shieldAbsorb = minOf(currentShield, rawDmg)
      val remainingDmg = rawDmg - shieldAbsorb
      val newShield = currentShield - shieldAbsorb
      val newHp = (p.hullHp - remainingDmg).coerceAtLeast(0)

      particleManager.triggerShake(intensity = 18f, duration = 0.4f)
      vibrate(90)

      // Enemy passive mechanic: corrupt a tile
      val corruptedCoord = Match3Engine.corruptRandomTile(_uiState.value.board)
      val updatedBoard = if (corruptedCoord != null) {
        val b = Array(Match3Engine.ROWS) { r ->
          Array(Match3Engine.COLS) { c ->
            _uiState.value.board[r][c].copy()
          }
        }
        b[corruptedCoord.first][corruptedCoord.second] = b[corruptedCoord.first][corruptedCoord.second].copy(
          special = SpecialType.CORRUPTED
        )
        b
      } else {
        _uiState.value.board
      }

      _uiState.update {
        it.copy(
          board = updatedBoard,
          currentEnemy = enemy.copy(currentCooldown = enemy.maxCooldown),
          player = p.copy(hullHp = newHp, shield = newShield),
          statusMessage = "${enemy.name} attacked for $rawDmg damage! Node corrupted."
        )
      }

      if (newHp <= 0) {
        handleCombatDefeat("Chassis destroyed by ${enemy.name}.")
      }
    } else {
      _uiState.update {
        it.copy(
          currentEnemy = enemy.copy(currentCooldown = newCooldown),
          statusMessage = "${enemy.name} charging attack (${newCooldown} turns left)"
        )
      }
    }

    // Verify if board has possible moves, if not reshuffle
    if (!Match3Engine.hasPossibleMoves(_uiState.value.board)) {
      val reshuffled = Match3Engine.reshuffleBoard(_uiState.value.board)
      particleManager.triggerShake(intensity = 10f, duration = 0.3f)
      _uiState.update {
        it.copy(
          board = reshuffled,
          statusMessage = "NO MOVES DETECTED: Cyber Matrix Reshuffled!"
        )
      }
    }
  }

  private fun handleCombatVictory(enemy: Enemy) {
    val p = _uiState.value.player
    val hasCreditSniffer = p.relics.any { it.id == "relic_credit_sniffer" }

    val baseCreds = if (enemy.isBoss) 120 else if (enemy.isElite) 65 else 35
    val rewardCredits = if (hasCreditSniffer) (baseCreds * 1.5f).toInt() else baseCreds
    val rewardShards = if (enemy.isBoss) 40 else if (enemy.isElite) 20 else 8

    metaRepo.addNeonShards(rewardShards)
    metaRepo.recordScore(p.score + 1000)

    val updatedPlayer = p.copy(
      credits = p.credits + rewardCredits,
      neonShards = p.neonShards + rewardShards,
      score = p.score + (if (enemy.isBoss) 5000 else 1500)
    )

    if (enemy.isBoss) {
      metaRepo.incrementRunsCompleted()
      _uiState.update {
        it.copy(
          player = updatedPlayer,
          totalNeonShards = metaRepo.getNeonShards(),
          highScore = metaRepo.getHighScore(),
          currentScreen = GameScreen.VICTORY,
          victoryMessage = "ARCHON OVERMIND NEUTRALIZED! Sector Cleansed."
        )
      }
    } else {
      advanceSectorMap()
      _uiState.update {
        it.copy(
          player = updatedPlayer,
          totalNeonShards = metaRepo.getNeonShards(),
          highScore = metaRepo.getHighScore(),
          lastCombatRewardCredits = rewardCredits,
          lastCombatRewardShards = rewardShards,
          currentScreen = GameScreen.SECTOR_MAP,
          statusMessage = "VICTORY: ${enemy.name} Destroyed! +$rewardCredits Credits, +$rewardShards Shards."
        )
      }
    }
    vibrate(80)
  }

  private fun handleCombatDefeat(reason: String) {
    val p = _uiState.value.player
    metaRepo.recordScore(p.score)
    metaRepo.addNeonShards(p.neonShards)
    _uiState.update {
      it.copy(
        totalNeonShards = metaRepo.getNeonShards(),
        highScore = metaRepo.getHighScore(),
        currentScreen = GameScreen.GAME_OVER,
        statusMessage = reason
      )
    }
    vibrate(150)
  }

  // Active Ability Triggers
  fun activateAbility(ability: CyberAbility) {
    val p = _uiState.value.player
    if (p.energy < ability.energyCost || _uiState.value.isCascading) return

    when (ability.type) {
      AbilityType.EMP_PULSE -> {
        // Clear all corrupted nodes + reshuffle
        val newBoard = Match3Engine.reshuffleBoard(_uiState.value.board)
        _uiState.update {
          it.copy(
            board = newBoard,
            player = p.copy(energy = p.energy - ability.energyCost),
            statusMessage = "EMP SURGE ACTIVATED: Board purged and reshuffled!"
          )
        }
        particleManager.triggerShake(intensity = 15f, duration = 0.35f)
        vibrate(60)
      }
      AbilityType.ORBITAL_LASER -> {
        _uiState.update {
          it.copy(
            targetingAction = TargetingAction.ORBITAL_LASER,
            statusMessage = "Select target tile for Orbital Ion Laser sweep."
          )
        }
      }
      AbilityType.QUANTUM_SWAP -> {
        _uiState.update {
          it.copy(
            targetingAction = TargetingAction.QUANTUM_SWAP_FIRST,
            statusMessage = "Select first tile for Quantum Swap."
          )
        }
      }
      AbilityType.OVERCLOCK_TURN -> {
        _uiState.update {
          it.copy(
            player = p.copy(
              energy = p.energy - ability.energyCost,
              movesRemaining = p.movesRemaining + 3,
              damageMultiplier = p.damageMultiplier + 0.5f
            ),
            statusMessage = "Overclock Hack: +3 Moves, +50% Damage!"
          )
        }
        vibrate(40)
      }
      AbilityType.NOVA_SYNTHESIS -> {
        _uiState.update {
          it.copy(
            targetingAction = TargetingAction.NOVA_SYNTHESIS,
            statusMessage = "Select tile to convert into Rainbow Hyper Nova."
          )
        }
      }
    }
  }

  private fun applyOrbitalLaser(targetR: Int, targetC: Int, cellSizePx: Float, boardOrigin: Offset) {
    val p = _uiState.value.player
    val cost = 35
    if (p.energy < cost) return

    val toClear = mutableSetOf<Pair<Int, Int>>()
    for (c in 0 until Match3Engine.COLS) toClear.add(Pair(targetR, c))
    for (r in 0 until Match3Engine.ROWS) toClear.add(Pair(r, targetC))

    val center = Offset(
      boardOrigin.x + targetC * cellSizePx + cellSizePx / 2f,
      boardOrigin.y + targetR * cellSizePx + cellSizePx / 2f
    )
    particleManager.spawnLaser(
      Offset(boardOrigin.x, center.y),
      Offset(boardOrigin.x + Match3Engine.COLS * cellSizePx, center.y),
      NeonCyan,
      isHorizontal = true
    )
    particleManager.spawnLaser(
      Offset(center.x, boardOrigin.y),
      Offset(center.x, boardOrigin.y + Match3Engine.ROWS * cellSizePx),
      NeonMagenta,
      isHorizontal = false
    )
    particleManager.triggerShake(intensity = 16f, duration = 0.35f)

    val updatedBoard = Match3Engine.applyCascade(_uiState.value.board, toClear, emptyMap())
    val curEnemy = _uiState.value.currentEnemy
    val laserDamage = 90
    val newEnemyHp = if (curEnemy != null) (curEnemy.currentHp - laserDamage).coerceAtLeast(0) else 0

    _uiState.update {
      it.copy(
        board = updatedBoard,
        targetingAction = TargetingAction.NONE,
        player = p.copy(energy = p.energy - cost, score = p.score + 1200),
        currentEnemy = curEnemy?.copy(currentHp = newEnemyHp),
        statusMessage = "Orbital Laser vaporized crossbeam (-$laserDamage HP)!"
      )
    }
    vibrate(70)

    if (newEnemyHp <= 0 && curEnemy != null) {
      handleCombatVictory(curEnemy)
    }
  }

  private fun applyNovaSynthesis(targetR: Int, targetC: Int, cellSizePx: Float, boardOrigin: Offset) {
    val p = _uiState.value.player
    val cost = 45
    if (p.energy < cost) return

    val copyBoard = Array(Match3Engine.ROWS) { r ->
      Array(Match3Engine.COLS) { c ->
        _uiState.value.board[r][c].copy()
      }
    }
    copyBoard[targetR][targetC] = copyBoard[targetR][targetC].copy(special = SpecialType.COLOR_NOVA)

    val center = Offset(
      boardOrigin.x + targetC * cellSizePx + cellSizePx / 2f,
      boardOrigin.y + targetR * cellSizePx + cellSizePx / 2f
    )
    particleManager.spawnShockwave(center, NeonAmber)

    _uiState.update {
      it.copy(
        board = copyBoard,
        targetingAction = TargetingAction.NONE,
        player = p.copy(energy = p.energy - cost),
        statusMessage = "Hyper Nova Synthesized!"
      )
    }
    vibrate(50)
  }

  // Meta Progression Citadel Upgrades
  fun purchaseCitadelUpgrade(upgrade: GameContent.MetaUpgrade) {
    if (upgrade.isMaxed) return
    val cost = upgrade.nextCost
    if (metaRepo.spendNeonShards(cost)) {
      metaRepo.incrementUpgradeLevel(upgrade.id)
      _uiState.update {
        it.copy(
          metaUpgrades = loadMetaUpgrades(),
          totalNeonShards = metaRepo.getNeonShards(),
          statusMessage = "Upgraded: ${upgrade.name}!"
        )
      }
      vibrate(40)
    }
  }

  private fun vibrate(durationMs: Long) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(durationMs)
      }
    } catch (_: Exception) {
      // Ignore vibration errors on environments without vibrator
    }
  }
}
