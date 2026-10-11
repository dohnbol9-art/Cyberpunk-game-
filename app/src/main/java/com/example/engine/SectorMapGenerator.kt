package com.example.engine

import com.example.model.*
import kotlin.random.Random

object SectorMapGenerator {

  fun generateSector(sectorIndex: Int): SectorMap {
    val nodes = mutableListOf<MapNode>()

    // Depth 0: 3 Battle nodes
    val depth0 = listOf(
      MapNode(id = "n_0_0", depth = 0, lane = 0, type = NodeType.BATTLE, isAvailable = true),
      MapNode(id = "n_0_1", depth = 0, lane = 1, type = NodeType.BATTLE, isAvailable = true),
      MapNode(id = "n_0_2", depth = 0, lane = 2, type = NodeType.BATTLE, isAvailable = true)
    )

    // Depth 1: Battle or Event
    val depth1 = listOf(
      MapNode(id = "n_1_0", depth = 1, lane = 0, type = if (Random.nextBoolean()) NodeType.BATTLE else NodeType.EVENT),
      MapNode(id = "n_1_1", depth = 1, lane = 1, type = NodeType.BATTLE),
      MapNode(id = "n_1_2", depth = 1, lane = 2, type = if (Random.nextBoolean()) NodeType.BATTLE else NodeType.EVENT)
    )

    // Depth 2: Shop or Battle
    val depth2 = listOf(
      MapNode(id = "n_2_0", depth = 2, lane = 0, type = NodeType.SHOP),
      MapNode(id = "n_2_1", depth = 2, lane = 1, type = NodeType.BATTLE),
      MapNode(id = "n_2_2", depth = 2, lane = 2, type = NodeType.EVENT)
    )

    // Depth 3: Elite Guardian
    val depth3 = listOf(
      MapNode(id = "n_3_0", depth = 3, lane = 0, type = NodeType.ELITE),
      MapNode(id = "n_3_1", depth = 3, lane = 2, type = NodeType.ELITE)
    )

    // Depth 4: Rest / Nanite chamber or Shop
    val depth4 = listOf(
      MapNode(id = "n_4_0", depth = 4, lane = 0, type = NodeType.REST),
      MapNode(id = "n_4_1", depth = 4, lane = 1, type = NodeType.SHOP),
      MapNode(id = "n_4_2", depth = 4, lane = 2, type = NodeType.REST)
    )

    // Depth 5: Elite or Pre-Boss battle
    val depth5 = listOf(
      MapNode(id = "n_5_0", depth = 5, lane = 0, type = NodeType.BATTLE),
      MapNode(id = "n_5_1", depth = 5, lane = 2, type = NodeType.ELITE)
    )

    // Depth 6: Sector Core Boss
    val depth6 = listOf(
      MapNode(id = "n_6_0", depth = 6, lane = 1, type = NodeType.BOSS)
    )

    // Connect layers
    val d0Linked = depth0.map { n ->
      n.copy(nextNodeIds = depth1.map { it.id }.filter { kotlin.math.abs(it.takeLast(1).toInt() - n.lane) <= 1 })
    }
    val d1Linked = depth1.map { n ->
      n.copy(nextNodeIds = depth2.map { it.id }.filter { kotlin.math.abs(it.takeLast(1).toInt() - n.lane) <= 1 })
    }
    val d2Linked = depth2.map { n ->
      // To 2 elite nodes
      n.copy(nextNodeIds = depth3.map { it.id })
    }
    val d3Linked = depth3.map { n ->
      n.copy(nextNodeIds = depth4.map { it.id })
    }
    val d4Linked = depth4.map { n ->
      n.copy(nextNodeIds = depth5.map { it.id })
    }
    val d5Linked = depth5.map { n ->
      n.copy(nextNodeIds = listOf(depth6.first().id))
    }

    nodes.addAll(d0Linked)
    nodes.addAll(d1Linked)
    nodes.addAll(d2Linked)
    nodes.addAll(d3Linked)
    nodes.addAll(d4Linked)
    nodes.addAll(d5Linked)
    nodes.addAll(depth6)

    return SectorMap(nodes = nodes, currentDepth = 0)
  }
}
