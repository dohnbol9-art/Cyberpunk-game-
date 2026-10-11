package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState

@Composable
fun EndRunScreen(
  uiState: GameUiState,
  isVictory: Boolean,
  onReturnHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  val titleColor = if (isVictory) NeonCyan else CyberHealth
  val titleText = if (isVictory) "SECTOR OVERLORD DEFEATED" else "MATRIX COLLAPSE"
  val subText = if (isVictory) uiState.victoryMessage else uiState.statusMessage

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberDarkBg)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      Icon(
        imageVector = if (isVictory) Icons.Default.EmojiEvents else Icons.Default.Dangerous,
        contentDescription = "Status",
        tint = titleColor,
        modifier = Modifier.size(56.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = titleText,
        color = titleColor,
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = subText,
        color = TextSecondary,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Stats Summary Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatRow("FINAL SCORE", "${uiState.player.score}", NeonAmber)
          StatRow("TOTAL DAMAGE DEALT", "${uiState.player.totalDamageDealt}", NeonMagenta)
          StatRow("MAX COMBO CHAIN", "x${uiState.player.maxCombosAchieved}", NeonCyan)
          StatRow("RELICS DISCOVERED", "${uiState.player.relics.size}", NeonGreen)
          Divider(color = CyberBorder, thickness = 0.8.dp)
          StatRow("NEON SHARDS RECOVERED", "+${uiState.player.neonShards}", NeonCyan, isBold = true)
        }
      }
    }

    Button(
      onClick = onReturnHome,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("return_home_button"),
      colors = ButtonDefaults.buttonColors(containerColor = if (isVictory) NeonCyan else CyberSurfaceVariant),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isVictory) NeonCyan else CyberBorderGlow),
      shape = RoundedCornerShape(12.dp)
    ) {
      Text(
        text = "RETURN TO MAINFRAME",
        color = if (isVictory) Color.Black else NeonCyan,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun StatRow(
  label: String,
  value: String,
  color: Color,
  isBold: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = TextMuted,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      fontFamily = FontFamily.Monospace
    )
    Text(
      text = value,
      color = color,
      fontSize = if (isBold) 14.sp else 12.sp,
      fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
  }
}
