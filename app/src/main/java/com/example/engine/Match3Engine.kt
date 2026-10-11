package com.example.engine

import com.example.model.*
import kotlin.random.Random

object Match3Engine {
  const val ROWS = 7
  const val COLS = 7

  val AVAILABLE_TYPES = listOf(
    GemType.CYAN_PRISM,
    GemType.MAGENTA_HEART,
    GemType.AMBER_STAR,
    GemType.ACID_EMERALD,
    GemType.VIOLET_ORB,
    GemType.ORANGE_FLAME
  )

  fun createInitialBoard(): Array<Array<Gem>> {
    val board = Array(ROWS) { r ->
      Array(COLS) { c ->
        Gem(type = AVAILABLE_TYPES.random(), row = r, col = c)
      }
    }

    // Ensure no pre-existing matches on initial generation
    var attempts = 0
    while (hasAnyMatches(board) && attempts < 100) {
      for (r in 0 until ROWS) {
        for (c in 0 until COLS) {
          if (isPartOfMatch(board, r, c)) {
            val disallowed = mutableSetOf<GemType>()
            if (c >= 2 && board[r][c - 1].type == board[r][c - 2].type) disallowed.add(board[r][c - 1].type)
            if (r >= 2 && board[r - 1][c].type == board[r - 2][c].type) disallowed.add(board[r - 1][c].type)
            val pool = AVAILABLE_TYPES.filter { it !in disallowed }
            board[r][c] = Gem(
              type = if (pool.isNotEmpty()) pool.random() else AVAILABLE_TYPES.random(),
              row = r,
              col = c
            )
          }
        }
      }
      attempts++
    }
    return board
  }

  private fun isPartOfMatch(board: Array<Array<Gem>>, r: Int, c: Int): Boolean {
    val type = board[r][c].type
    // Horizontal
    var hCount = 1
    var left = c - 1
    while (left >= 0 && board[r][left].type == type) { hCount++; left-- }
    var right = c + 1
    while (right < COLS && board[r][right].type == type) { hCount++; right++ }
    if (hCount >= 3) return true

    // Vertical
    var vCount = 1
    var up = r - 1
    while (up >= 0 && board[up][c].type == type) { vCount++; up-- }
    var down = r + 1
    while (down < ROWS && board[down][c].type == type) { vCount++; down++ }
    return vCount >= 3
  }

  fun hasAnyMatches(board: Array<Array<Gem>>): Boolean {
    for (r in 0 until ROWS) {
      for (c in 0 until COLS) {
        if (board[r][c].special == SpecialType.CORRUPTED) continue
        val type = board[r][c].type
        if (c + 2 < COLS && board[r][c + 1].type == type && board[r][c + 2].type == type) return true
        if (r + 2 < ROWS && board[r + 1][c].type == type && board[r + 2][c].type == type) return true
      }
    }
    return false
  }

  fun areAdjacent(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
    val dr = kotlin.math.abs(r1 - r2)
    val dc = kotlin.math.abs(c1 - c2)
    return (dr == 1 && dc == 0) || (dr == 0 && dc == 1)
  }

  /**
   * Evaluates matches, special triggers, combos, and returns the step result.
   */
  fun evaluateBoardStep(
    board: Array<Array<Gem>>,
    lastSwappedCoords: Pair<Pair<Int, Int>, Pair<Int, Int>>?,
    relics: List<Relic>,
    comboIndex: Int,
    damageMultiplier: Float
  ): StepCascadeResult {
    val toClear = mutableSetOf<Pair<Int, Int>>()
    val specialsToCreate = mutableMapOf<Pair<Int, Int>, Pair<GemType, SpecialType>>()
    var baseScore = 0
    var baseDamage = 0
    var shieldGained = 0
    var healGained = 0
    var energyGained = 0

    val hasPrismSplitter = relics.any { it.id == "relic_prism_splitter" }
    val hasNaniteRegen = relics.any { it.id == "relic_nanite_regen" }
    val hasAegisMatrix = relics.any { it.id == "relic_aegis_matrix" }
    val hasSolarOverload = relics.any { it.id == "relic_solar_overload" }
    val hasCritProtocol = relics.any { it.id == "relic_crit_protocol" }
    val hasPlasmaDynamo = relics.any { it.id == "relic_plasma_dynamo" }
    val hasGlitchRecycler = relics.any { it.id == "relic_glitch_recycler" }

    // Check Nova swap special interaction
    if (lastSwappedCoords != null && comboIndex == 1) {
      val (pos1, pos2) = lastSwappedCoords
      val gem1 = board[pos1.first][pos1.second]
      val gem2 = board[pos2.first][pos2.second]

      if (gem1.special == SpecialType.COLOR_NOVA && gem2.special == SpecialType.COLOR_NOVA) {
        // Double Nova! Clear entire board!
        for (r in 0 until ROWS) {
          for (c in 0 until COLS) {
            toClear.add(Pair(r, c))
          }
        }
        baseScore += 5000
        baseDamage += 250
        energyGained += 40
      } else if (gem1.special == SpecialType.COLOR_NOVA || gem2.special == SpecialType.COLOR_NOVA) {
        val targetColor = if (gem1.special == SpecialType.COLOR_NOVA) gem2.type else gem1.type
        toClear.add(pos1)
        toClear.add(pos2)
        for (r in 0 until ROWS) {
          for (c in 0 until COLS) {
            if (board[r][c].type == targetColor) {
              toClear.add(Pair(r, c))
            }
          }
        }
        baseScore += 2000
        baseDamage += 120
        energyGained += 25
      }
    }

    // Standard run detection
    val horizontalMatches = mutableListOf<List<Pair<Int, Int>>>()
    for (r in 0 until ROWS) {
      var c = 0
      while (c < COLS) {
        if (board[r][c].special == SpecialType.CORRUPTED) {
          c++
          continue
        }
        val type = board[r][c].type
        val run = mutableListOf(Pair(r, c))
        var nextC = c + 1
        while (nextC < COLS && board[r][nextC].type == type && board[r][nextC].special != SpecialType.CORRUPTED) {
          run.add(Pair(r, nextC))
          nextC++
        }
        if (run.size >= 3) {
          horizontalMatches.add(run)
        }
        c = nextC
      }
    }

    val verticalMatches = mutableListOf<List<Pair<Int, Int>>>()
    for (c in 0 until COLS) {
      var r = 0
      while (r < ROWS) {
        if (board[r][c].special == SpecialType.CORRUPTED) {
          r++
          continue
        }
        val type = board[r][c].type
        val run = mutableListOf(Pair(r, c))
        var nextR = r + 1
        while (nextR < ROWS && board[nextR][c].type == type && board[nextR][c].special != SpecialType.CORRUPTED) {
          run.add(Pair(nextR, c))
          nextR++
        }
        if (run.size >= 3) {
          verticalMatches.add(run)
        }
        r = nextR
      }
    }

    // Process matched coordinates & determine special creations
    for (hRun in horizontalMatches) {
      toClear.addAll(hRun)
      val gemType = board[hRun.first().first][hRun.first().second].type
      val spawnPos = determineSpecialSpawnPos(hRun, lastSwappedCoords)
      if (hRun.size == 5) {
        specialsToCreate[spawnPos] = Pair(gemType, SpecialType.COLOR_NOVA)
      } else if (hRun.size == 4) {
        val special = if (hasPrismSplitter) SpecialType.NEON_BOMB else SpecialType.BEAM_VERTICAL
        specialsToCreate[spawnPos] = Pair(gemType, special)
      }
    }

    for (vRun in verticalMatches) {
      toClear.addAll(vRun)
      val gemType = board[vRun.first().first][vRun.first().second].type
      val spawnPos = determineSpecialSpawnPos(vRun, lastSwappedCoords)
      if (vRun.size == 5) {
        specialsToCreate[spawnPos] = Pair(gemType, SpecialType.COLOR_NOVA)
      } else if (vRun.size == 4) {
        val special = if (hasPrismSplitter) SpecialType.NEON_BOMB else SpecialType.BEAM_HORIZONTAL
        specialsToCreate[spawnPos] = Pair(gemType, special)
      }
    }

    // Check intersections (T/L shape -> Neon Bomb)
    for (hRun in horizontalMatches) {
      for (vRun in verticalMatches) {
        val intersect = hRun.intersect(vRun.toSet())
        if (intersect.isNotEmpty()) {
          val pivot = intersect.first()
          val gemType = board[pivot.first][pivot.second].type
          specialsToCreate[pivot] = Pair(gemType, SpecialType.NEON_BOMB)
        }
      }
    }

    // Now trigger secondary special expansions from any matched specials
    val queue = ArrayDeque(toClear)
    val processedSpecials = mutableSetOf<Pair<Int, Int>>()

    while (queue.isNotEmpty()) {
      val coord = queue.removeFirst()
      if (coord in processedSpecials) continue
      val gem = board[coord.first][coord.second]

      when (gem.special) {
        SpecialType.BEAM_HORIZONTAL -> {
          processedSpecials.add(coord)
          for (c in 0 until COLS) {
            val target = Pair(coord.first, c)
            if (target !in toClear) {
              toClear.add(target)
              queue.add(target)
            }
          }
          baseScore += 300
          baseDamage += 25
        }
        SpecialType.BEAM_VERTICAL -> {
          processedSpecials.add(coord)
          for (r in 0 until ROWS) {
            val target = Pair(r, coord.second)
            if (target !in toClear) {
              toClear.add(target)
              queue.add(target)
            }
          }
          baseScore += 300
          baseDamage += 25
        }
        SpecialType.NEON_BOMB -> {
          processedSpecials.add(coord)
          for (dr in -1..1) {
            for (dc in -1..1) {
              val nr = coord.first + dr
              val nc = coord.second + dc
              if (nr in 0 until ROWS && nc in 0 until COLS) {
                val target = Pair(nr, nc)
                if (target !in toClear) {
                  toClear.add(target)
                  queue.add(target)
                }
              }
            }
          }
          baseScore += 500
          baseDamage += 40
        }
        SpecialType.COLOR_NOVA -> {
          processedSpecials.add(coord)
          val randomColor = AVAILABLE_TYPES.random()
          for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
              if (board[r][c].type == randomColor) {
                val target = Pair(r, c)
                if (target !in toClear) {
                  toClear.add(target)
                  queue.add(target)
                }
              }
            }
          }
          baseScore += 1200
          baseDamage += 80
        }
        else -> Unit
      }
    }

    // Clear adjacent corrupted nodes
    val purgedGlitchNodes = mutableListOf<Pair<Int, Int>>()
    for (coord in toClear) {
      for (dr in listOf(-1, 1, 0, 0)) {
        for (dc in listOf(0, 0, -1, 1)) {
          val nr = coord.first + dr
          val nc = coord.second + dc
          if (nr in 0 until ROWS && nc in 0 until COLS) {
            if (board[nr][nc].special == SpecialType.CORRUPTED) {
              purgedGlitchNodes.add(Pair(nr, nc))
            }
          }
        }
      }
    }
    for (glitch in purgedGlitchNodes) {
      toClear.add(glitch)
      baseScore += 150
      if (hasGlitchRecycler) {
        healGained += 12
        energyGained += 15
      }
    }

    // Calculate score, damage, heals, shields per gem matched
    for (coord in toClear) {
      val gem = board[coord.first][coord.second]
      var gemDmg = 8
      baseScore += 60

      when (gem.type) {
        GemType.CYAN_PRISM -> {
          if (hasAegisMatrix) shieldGained += 3
          gemDmg += 2
        }
        GemType.MAGENTA_HEART -> {
          healGained += 1
          gemDmg += 2
        }
        GemType.AMBER_STAR -> {
          if (hasSolarOverload) gemDmg = (gemDmg * 1.6f).toInt()
        }
        GemType.ACID_EMERALD -> {
          if (hasNaniteRegen) healGained += 3
        }
        GemType.VIOLET_ORB -> {
          val energyAdd = if (hasPlasmaDynamo) 4 else 2
          energyGained += energyAdd
        }
        GemType.ORANGE_FLAME -> {
          gemDmg += 5
        }
      }
      baseDamage += gemDmg
    }

    // Critical roll
    val isCritical = hasCritProtocol && Random.nextFloat() < 0.25f
    if (isCritical) {
      baseDamage = (baseDamage * 2.5f).toInt()
      baseScore = (baseScore * 2.5f).toInt()
    }

    // Apply combo multiplier and player damage multiplier
    val comboMultiplier = 1.0f + (comboIndex - 1) * 0.4f
    val finalDamage = (baseDamage * comboMultiplier * damageMultiplier).toInt()
    val finalScore = (baseScore * comboMultiplier).toInt()

    return StepCascadeResult(
      matchedCoords = toClear,
      createdSpecials = specialsToCreate,
      baseScore = finalScore,
      damageDealt = finalDamage,
      shieldGained = shieldGained,
      healGained = healGained,
      energyGained = energyGained,
      isCritical = isCritical,
      comboStep = comboIndex
    )
  }

  private fun determineSpecialSpawnPos(
    run: List<Pair<Int, Int>>,
    lastSwappedCoords: Pair<Pair<Int, Int>, Pair<Int, Int>>?
  ): Pair<Int, Int> {
    if (lastSwappedCoords != null) {
      if (lastSwappedCoords.first in run) return lastSwappedCoords.first
      if (lastSwappedCoords.second in run) return lastSwappedCoords.second
    }
    return run[run.size / 2]
  }

  /**
   * Applies the cascade collapse downward and returns a newly populated board.
   */
  fun applyCascade(
    board: Array<Array<Gem>>,
    clearedCoords: Set<Pair<Int, Int>>,
    specialsToCreate: Map<Pair<Int, Int>, Pair<GemType, SpecialType>>
  ): Array<Array<Gem>> {
    val newBoard = Array(ROWS) { r ->
      Array(COLS) { c ->
        board[r][c].copy()
      }
    }

    // Create special gems before falling
    for ((coord, specPair) in specialsToCreate) {
      newBoard[coord.first][coord.second] = Gem(
        type = specPair.first,
        special = specPair.second,
        row = coord.first,
        col = coord.second
      )
    }

    // Gravity per column
    for (c in 0 until COLS) {
      val survivingGems = mutableListOf<Gem>()
      for (r in ROWS - 1 downTo 0) {
        val coord = Pair(r, c)
        if (coord !in clearedCoords || coord in specialsToCreate) {
          survivingGems.add(newBoard[r][c])
        }
      }

      var writeRow = ROWS - 1
      for (gem in survivingGems) {
        newBoard[writeRow][c] = gem.copyWithPosition(writeRow, c)
        writeRow--
      }

      // Fill remaining top slots with new gems
      while (writeRow >= 0) {
        newBoard[writeRow][c] = Gem(
          type = AVAILABLE_TYPES.random(),
          row = writeRow,
          col = c
        )
        writeRow--
      }
    }

    return newBoard
  }

  /**
   * Checks if any valid swap can be made on the board.
   */
  fun hasPossibleMoves(board: Array<Array<Gem>>): Boolean {
    // Check horizontal swaps
    for (r in 0 until ROWS) {
      for (c in 0 until COLS - 1) {
        if (canSwapMakeMatch(board, r, c, r, c + 1)) return true
      }
    }
    // Check vertical swaps
    for (r in 0 until ROWS - 1) {
      for (c in 0 until COLS) {
        if (canSwapMakeMatch(board, r, c, r + 1, c)) return true
      }
    }
    return false
  }

  fun canSwapMakeMatch(board: Array<Array<Gem>>, r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
    val g1 = board[r1][c1]
    val g2 = board[r2][c2]
    if (g1.special == SpecialType.COLOR_NOVA || g2.special == SpecialType.COLOR_NOVA) return true
    if (g1.special == SpecialType.CORRUPTED || g2.special == SpecialType.CORRUPTED) return false

    // Virtual swap
    val copyBoard = Array(ROWS) { r ->
      Array(COLS) { c ->
        board[r][c]
      }
    }
    copyBoard[r1][c1] = g2
    copyBoard[r2][c2] = g1

    return isPartOfMatch(copyBoard, r1, c1) || isPartOfMatch(copyBoard, r2, c2)
  }

  /**
   * Reshuffle the board with guarantee of possible moves and no initial matches.
   */
  fun reshuffleBoard(board: Array<Array<Gem>>): Array<Array<Gem>> {
    val allGems = mutableListOf<GemType>()
    for (r in 0 until ROWS) {
      for (c in 0 until COLS) {
        if (board[r][c].special != SpecialType.CORRUPTED) {
          allGems.add(board[r][c].type)
        }
      }
    }
    allGems.shuffle()

    var attempts = 0
    var candidate: Array<Array<Gem>>
    do {
      var gemIdx = 0
      candidate = Array(ROWS) { r ->
        Array(COLS) { c ->
          if (board[r][c].special == SpecialType.CORRUPTED) {
            board[r][c]
          } else {
            val type = if (gemIdx < allGems.size) allGems[gemIdx++] else AVAILABLE_TYPES.random()
            Gem(type = type, row = r, col = c)
          }
        }
      }
      attempts++
    } while ((hasAnyMatches(candidate) || !hasPossibleMoves(candidate)) && attempts < 50)

    if (!hasPossibleMoves(candidate)) {
      return createInitialBoard()
    }
    return candidate
  }

  fun corruptRandomTile(board: Array<Array<Gem>>): Pair<Int, Int>? {
    val validCoords = mutableListOf<Pair<Int, Int>>()
    for (r in 0 until ROWS) {
      for (c in 0 until COLS) {
        if (board[r][c].special == SpecialType.NONE) {
          validCoords.add(Pair(r, c))
        }
      }
    }
    return validCoords.randomOrNull()
  }
}
