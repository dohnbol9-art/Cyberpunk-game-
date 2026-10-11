package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameContent
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState

@Composable
fun CitadelScreen(
  uiState: GameUiState,
  onBuyUpgrade: (GameContent.MetaUpgrade) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberDarkBg)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("citadel_back_button")
        ) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NeonCyan)
        }

        Surface(
          color = CyberSurfaceVariant,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Diamond, contentDescription = "Shards", tint = NeonCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${uiState.totalNeonShards} NEON SHARDS",
              color = NeonCyan,
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "CYBERSYSTEM CITADEL",
        color = NeonMagenta,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
      Text(
        text = "Permanently augment your matrix vessel across all future runs.",
        color = TextSecondary,
        fontSize = 12.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.heightIn(max = 480.dp)
      ) {
        items(uiState.metaUpgrades) { upgrade ->
          val cost = upgrade.nextCost
          val canAfford = uiState.totalNeonShards >= cost && !upgrade.isMaxed

          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (canAfford) NeonCyan.copy(alpha = 0.5f) else CyberBorder)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = upgrade.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "RANK ${upgrade.currentLevel}/${upgrade.maxLevel}",
                    color = NeonAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = upgrade.description, color = TextSecondary, fontSize = 11.sp)
              }

              Button(
                onClick = { onBuyUpgrade(upgrade) },
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                  containerColor = NeonCyan,
                  contentColor = Color.Black,
                  disabledContainerColor = CyberSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("upgrade_${upgrade.id}")
              ) {
                Text(
                  text = if (upgrade.isMaxed) "MAX" else "$cost SHARDS",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }

    Button(
      onClick = onBack,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
      shape = RoundedCornerShape(10.dp)
    ) {
      Text(
        text = "RETURN TO TITLE",
        color = NeonCyan,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
