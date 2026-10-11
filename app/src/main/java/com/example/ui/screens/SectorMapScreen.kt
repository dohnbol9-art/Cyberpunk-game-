package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CyberPlayerHud
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState

@Composable
fun SectorMapScreen(
  uiState: GameUiState,
  onSelectNode: (MapNode) -> Unit,
  modifier: Modifier = Modifier
) {
  val map = uiState.sectorMap
  val nodesByDepth = remember(map.nodes) {
    map.nodes.groupBy { it.depth }.toSortedMap()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberDarkBg)
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    // Top Player HUD
    CyberPlayerHud(player = uiState.player)

    Spacer(modifier = Modifier.height(10.dp))

    // Sector Title Banner
    Surface(
      color = CyberSurface,
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "SECTOR 1: CYBER HIGHWAY",
            color = NeonCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Target: Archon Overmind Core",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.MonetizationOn, contentDescription = "Credits", tint = CyberGold, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "${uiState.player.credits}",
            color = CyberGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Map Nodes Progression List
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(vertical = 8.dp)
    ) {
      nodesByDepth.forEach { (depth, nodes) ->
        item {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "LAYER $depth ${if (depth == 6) "- SECTOR CORE" else ""}",
              color = if (depth == 6) NeonAmber else TextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              nodes.forEach { node ->
                MapNodeItem(
                  node = node,
                  onClick = { onSelectNode(node) }
                )
              }
            }
          }
        }
      }
    }

    // Bottom Status Message
    Surface(
      color = CyberSurfaceVariant,
      shape = RoundedCornerShape(8.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderGlow),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = uiState.statusMessage,
        color = TextPrimary,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(10.dp)
      )
    }
  }
}

@Composable
private fun MapNodeItem(
  node: MapNode,
  onClick: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val (icon, tintColor) = when (node.type) {
    NodeType.BATTLE -> Pair(Icons.Default.SportsEsports, NeonCyan)
    NodeType.ELITE -> Pair(Icons.Default.Security, NeonMagenta)
    NodeType.BOSS -> Pair(Icons.Default.Dangerous, NeonAmber)
    NodeType.SHOP -> Pair(Icons.Default.Storefront, CyberGold)
    NodeType.EVENT -> Pair(Icons.Default.HelpOutline, NeonPurple)
    NodeType.REST -> Pair(Icons.Default.LocalHospital, NeonGreen)
  }

  val isInteractive = node.isAvailable

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .testTag("node_${node.id}")
      .clickable(enabled = isInteractive) { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(if (node.type == NodeType.BOSS) 56.dp else 46.dp)
        .clip(CircleShape)
        .background(
          if (node.isCurrent) tintColor.copy(alpha = 0.35f)
          else if (node.isCleared) CyberSurface
          else if (node.isAvailable) tintColor.copy(alpha = 0.15f)
          else CyberSurface.copy(alpha = 0.4f)
        )
        .border(
          width = if (node.isCurrent || node.isAvailable) 2.dp else 1.dp,
          color = if (node.isCurrent) tintColor
          else if (node.isAvailable) tintColor
          else if (node.isCleared) TextMuted
          else CyberBorder,
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      if (node.isCleared) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Cleared",
          tint = TextMuted,
          modifier = Modifier.size(20.dp)
        )
      } else {
        Icon(
          imageVector = icon,
          contentDescription = node.type.label,
          tint = if (node.isAvailable || node.isCurrent) tintColor else TextMuted.copy(alpha = 0.4f),
          modifier = Modifier.size(if (node.type == NodeType.BOSS) 26.dp else 20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = node.type.label,
      color = if (node.isAvailable) tintColor else if (node.isCleared) TextMuted else TextMuted.copy(alpha = 0.5f),
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}
