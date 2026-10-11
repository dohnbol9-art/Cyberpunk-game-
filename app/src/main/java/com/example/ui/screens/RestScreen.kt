package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberPlayerHud
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState

@Composable
fun RestScreen(
  uiState: GameUiState,
  onRestHeal: () -> Unit,
  onRestOverclock: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberDarkBg)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      CyberPlayerHud(player = uiState.player)

      Spacer(modifier = Modifier.height(20.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonGreen)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "NANITE REGENERATION CHAMBER",
            color = NeonGreen,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "A tranquil node of clean power. Choose how to optimize your system core for the battles ahead.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
          )
        }
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      // Choice A: Nanite Heal
      Button(
        onClick = onRestHeal,
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp)
          .testTag("rest_heal_button"),
        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberHealth),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Start
        ) {
          Icon(Icons.Default.Healing, contentDescription = "Heal", tint = CyberHealth, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "NANITE REPAIR PROTOCOL",
              color = CyberHealth,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Repair Hull by +40% Max HP",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }
      }

      // Choice B: Overclock Core
      Button(
        onClick = onRestOverclock,
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp)
          .testTag("rest_overclock_button"),
        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberEnergy),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Start
        ) {
          Icon(Icons.Default.Speed, contentDescription = "Overclock", tint = CyberEnergy, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "CORE OVERCLOCK SURGE",
              color = CyberEnergy,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "+25% Permanent Combo Damage & Recharge Energy",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }
}
