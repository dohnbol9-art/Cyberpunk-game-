package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameContent
import com.example.ui.theme.*
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameUiState

@Composable
fun MainMenuScreen(
  uiState: GameUiState,
  onSelectClass: (GameContent.HeroClass) -> Unit,
  onStartRun: () -> Unit,
  onOpenCitadel: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "title_glow")
  val titleGlowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(CyberDarkBg, Color(0xFF0F0D24), CyberDarkBg)
        )
      )
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Header & Currency Stats
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // High Score
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.EmojiEvents, contentDescription = "High Score", tint = CyberGold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "BEST: ${uiState.highScore}",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }

        // Persistent Neon Shards
        Surface(
          color = CyberSurfaceVariant,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Diamond, contentDescription = "Neon Shards", tint = NeonCyan, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${uiState.totalNeonShards} SHARDS",
              color = NeonCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // App Title with Cyber Neon Glow
      Text(
        text = "NEON CRUSH",
        fontSize = 38.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp,
        color = NeonCyan,
        style = LocalTextStyle.current.copy(
          shadow = androidx.compose.ui.graphics.Shadow(
            color = NeonCyan.copy(alpha = titleGlowAlpha),
            blurRadius = 32f
          )
        )
      )

      Text(
        text = "ROGUE MATCH-3 PROTOCOL",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 4.sp,
        color = NeonMagenta,
        style = LocalTextStyle.current.copy(
          shadow = androidx.compose.ui.graphics.Shadow(
            color = NeonMagenta.copy(alpha = 0.7f),
            blurRadius = 16f
          )
        )
      )

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Synthesize combos, activate cybernetic relics, and breach the Archon Overmind core.",
        color = TextSecondary,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    // Class Selection Carousel
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 20.dp)
    ) {
      Text(
        text = "SELECT PROTOCOL CLASS",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(bottom = 10.dp)
      )

      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(GameContent.HERO_CLASSES) { heroClass ->
          val isSelected = uiState.selectedHeroClass.id == heroClass.id
          val accentColor = Color(heroClass.accentColorHex)

          Card(
            modifier = Modifier
              .width(260.dp)
              .clip(RoundedCornerShape(14.dp))
              .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else CyberBorder,
                shape = RoundedCornerShape(14.dp)
              )
              .clickable { onSelectClass(heroClass) }
              .testTag("class_${heroClass.id}"),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) CyberSurfaceVariant else CyberSurface
            ),
            shape = RoundedCornerShape(14.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = heroClass.name,
                  color = if (isSelected) accentColor else TextPrimary,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }

              Text(
                text = heroClass.title,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )

              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = heroClass.description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
              )

              Spacer(modifier = Modifier.height(10.dp))
              Divider(color = CyberBorder, thickness = 0.8.dp)
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "HP: ${heroClass.startingHp}", color = CyberHealth, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "EN: ${heroClass.startingEnergy}", color = CyberEnergy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = heroClass.startingAbility.name, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // Action Buttons
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Button(
        onClick = onStartRun,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("start_run_button"),
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "INITIATE RUN",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        }
      }

      OutlinedButton(
        onClick = onOpenCitadel,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("citadel_button"),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonMagenta),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonMagenta),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AccountTree, contentDescription = "Citadel", tint = NeonMagenta, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "CYBER CITADEL (PERMANENT UPGRADES)",
            color = NeonMagenta,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
