package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameContent
import com.example.ui.components.CyberPlayerHud
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState

@Composable
fun EventScreen(
  uiState: GameUiState,
  onSelectOption: (GameContent.EventOption) -> Unit,
  modifier: Modifier = Modifier
) {
  val event = uiState.currentEvent

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberDarkBg)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      CyberPlayerHud(player = uiState.player)

      Spacer(modifier = Modifier.height(16.dp))

      if (event != null) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = CyberSurface),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonPurple)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.HelpOutline, contentDescription = "Event", tint = NeonPurple, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = event.title,
                color = NeonPurple,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = event.description,
              color = TextPrimary,
              fontSize = 13.sp,
              lineHeight = 18.sp
            )
          }
        }
      }
    }

    if (event != null) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Option A
        Button(
          onClick = { onSelectOption(event.optionA) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("event_option_a"),
          colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(14.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
              text = event.optionA.title,
              color = NeonCyan,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = event.optionA.description, color = TextSecondary, fontSize = 11.sp)
          }
        }

        // Option B
        Button(
          onClick = { onSelectOption(event.optionB) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("event_option_b"),
          colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(14.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
              text = event.optionB.title,
              color = NeonMagenta,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = event.optionB.description, color = TextSecondary, fontSize = 11.sp)
          }
        }
      }
    }
  }
}
