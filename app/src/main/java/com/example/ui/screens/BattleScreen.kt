package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.Match3Engine
import com.example.engine.ParticleManager
import com.example.model.CyberAbility
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState
import com.example.viewmodel.TargetingAction

@Composable
fun BattleScreen(
  uiState: GameUiState,
  particleManager: ParticleManager,
  onGemClick: (r: Int, c: Int, cellSizePx: Float, boardOrigin: Offset) -> Unit,
  onActivateAbility: (CyberAbility) -> Unit,
  onCancelTargeting: () -> Unit,
  modifier: Modifier = Modifier
) {
  val density = LocalDensity.current
  var boardOrigin by remember { mutableStateOf(Offset.Zero) }
  var cellSizePx by remember { mutableFloatStateOf(0f) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CyberDarkBg)
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Enemy HUD
      if (uiState.currentEnemy != null) {
        CyberEnemyCard(enemy = uiState.currentEnemy)
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 2. Targeting Action Warning Banner (if active)
      AnimatedVisibility(visible = uiState.targetingAction != TargetingAction.NONE) {
        Surface(
          color = NeonMagenta.copy(alpha = 0.2f),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = when (uiState.targetingAction) {
                TargetingAction.ORBITAL_LASER -> "ORBITAL LASER: Tap any cell to fire"
                TargetingAction.QUANTUM_SWAP_FIRST -> "QUANTUM SWAP: Tap 1st gem"
                TargetingAction.QUANTUM_SWAP_SECOND -> "QUANTUM SWAP: Tap 2nd gem"
                TargetingAction.NOVA_SYNTHESIS -> "NOVA SYNTHESIS: Tap cell to convert"
                else -> ""
              },
              color = NeonMagenta,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            IconButton(
              onClick = onCancelTargeting,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Cancel", tint = NeonMagenta)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 3. Match-3 Board Container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.0f)
          .offset(
            x = with(density) { particleManager.shakeOffset.x.toDp() },
            y = with(density) { particleManager.shakeOffset.y.toDp() }
          )
          .clip(RoundedCornerShape(16.dp))
          .background(CyberSurface)
          .border(1.5.dp, CyberBorderGlow, RoundedCornerShape(16.dp))
          .onGloballyPositioned { coordinates ->
            boardOrigin = coordinates.positionInRoot()
            cellSizePx = coordinates.size.width.toFloat() / Match3Engine.COLS
          }
          .padding(4.dp)
      ) {
        // Gem Grid
        Column(modifier = Modifier.fillMaxSize()) {
          for (r in 0 until Match3Engine.ROWS) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
              for (c in 0 until Match3Engine.COLS) {
                val gem = uiState.board[r][c]
                val isSelected = uiState.selectedGemCoord?.first == r && uiState.selectedGemCoord?.second == c
                val isTargetFirst = uiState.quantumFirstCoord?.first == r && uiState.quantumFirstCoord?.second == c

                NeonGemView(
                  gem = gem,
                  isSelected = isSelected || isTargetFirst,
                  onClick = { onGemClick(r, c, cellSizePx, boardOrigin) },
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("gem_${r}_${c}")
                )
              }
            }
          }
        }

        // Particle & Laser Overlay Canvas on top of the grid
        NeonEffectsCanvas(
          particleManager = particleManager,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // 4. Player HUD
      CyberPlayerHud(player = uiState.player)

      Spacer(modifier = Modifier.height(4.dp))

      // 5. Active Abilities Row
      CyberAbilitiesRow(
        abilities = uiState.player.abilities,
        playerEnergy = uiState.player.energy,
        isCascading = uiState.isCascading,
        onActivate = onActivateAbility
      )

      Spacer(modifier = Modifier.height(4.dp))

      // 6. Tactical Status Log Console
      Surface(
        color = CyberSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = uiState.statusMessage,
          color = TextSecondary,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          maxLines = 1,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }
  }
}
