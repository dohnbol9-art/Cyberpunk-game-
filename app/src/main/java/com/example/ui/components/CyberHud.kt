package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun CyberPlayerHud(
  player: PlayerState,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, CyberBorder, RoundedCornerShape(12.dp)),
    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.9f)),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
    ) {
      // Top stats: Hull HP & Shield | Energy | Moves
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Hull & Shield
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Hull",
            tint = CyberHealth,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${player.hullHp}/${player.maxHullHp}",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          if (player.shield > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = CyberShield.copy(alpha = 0.2f),
              shape = RoundedCornerShape(4.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, CyberShield)
            ) {
              Text(
                text = "+${player.shield} SHIELD",
                color = CyberShield,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }

        // Energy
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Energy",
            tint = CyberEnergy,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${player.energy}/${player.maxEnergy} EN",
            color = CyberEnergy,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        // Moves Counter Badge
        Surface(
          color = if (player.movesRemaining <= 5) CyberHealth.copy(alpha = 0.3f) else NeonCyan.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (player.movesRemaining <= 5) CyberHealth else NeonCyan
          )
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = "Moves",
              tint = if (player.movesRemaining <= 5) CyberHealth else NeonCyan,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${player.movesRemaining} MOVES",
              color = if (player.movesRemaining <= 5) CyberHealth else NeonCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Health Bar
      LinearProgressIndicator(
        progress = { player.hpPercent },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = CyberHealth,
        trackColor = CyberSurfaceVariant
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Energy Bar
      LinearProgressIndicator(
        progress = { player.energyPercent },
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = CyberEnergy,
        trackColor = CyberSurfaceVariant
      )

      // Relics Bar if any
      if (player.relics.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          player.relics.forEach { relic ->
            Surface(
              color = CyberSurfaceVariant,
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(0.8.dp, NeonMagenta.copy(alpha = 0.5f))
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Memory,
                  contentDescription = relic.name,
                  tint = NeonMagenta,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = relic.name,
                  color = TextSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CyberEnemyCard(
  enemy: Enemy,
  modifier: Modifier = Modifier
) {
  val isLowHp = enemy.hpPercent <= 0.3f
  val hpBarColor by animateColorAsState(
    targetValue = if (isLowHp) CyberHealth else NeonMagenta,
    label = "enemy_hp_color"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .border(
        1.5.dp,
        if (enemy.isBoss) NeonAmber else if (enemy.isElite) NeonMagenta else CyberBorder,
        RoundedCornerShape(12.dp)
      ),
    colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.95f)),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = enemy.name,
              color = if (enemy.isBoss) NeonAmber else TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (enemy.isBoss || enemy.isElite) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = if (enemy.isBoss) NeonAmber.copy(alpha = 0.2f) else NeonMagenta.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (enemy.isBoss) NeonAmber else NeonMagenta
                )
              ) {
                Text(
                  text = if (enemy.isBoss) "BOSS" else "ELITE",
                  color = if (enemy.isBoss) NeonAmber else NeonMagenta,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }
          Text(
            text = enemy.title,
            color = TextSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Enemy Intent / Attack countdown
        Surface(
          color = CyberHealth.copy(alpha = 0.2f),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, CyberHealth)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = "Attack intent",
              tint = CyberHealth,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "IN ${enemy.currentCooldown} TURNS: ${enemy.attackDamage} DMG",
              color = CyberHealth,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Enemy HP Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "CORE INTEGRITY",
          color = TextMuted,
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "${enemy.currentHp}/${enemy.maxHp}",
          color = hpBarColor,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(3.dp))

      LinearProgressIndicator(
        progress = { enemy.hpPercent },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = hpBarColor,
        trackColor = CyberSurfaceVariant
      )

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = enemy.passiveDescription,
        color = TextMuted,
        fontSize = 9.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun CyberAbilitiesRow(
  abilities: List<CyberAbility>,
  playerEnergy: Int,
  isCascading: Boolean,
  onActivate: (CyberAbility) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    abilities.forEach { ability ->
      val canAfford = playerEnergy >= ability.energyCost && !isCascading
      val icon = when (ability.type) {
        AbilityType.EMP_PULSE -> Icons.Default.FlashOn
        AbilityType.ORBITAL_LASER -> Icons.Default.CenterFocusStrong
        AbilityType.QUANTUM_SWAP -> Icons.Default.SwapHoriz
        AbilityType.OVERCLOCK_TURN -> Icons.Default.Speed
        AbilityType.NOVA_SYNTHESIS -> Icons.Default.AutoAwesome
      }

      Button(
        onClick = { onActivate(ability) },
        enabled = canAfford,
        modifier = Modifier
          .height(38.dp)
          .testTag("ability_${ability.type.name.lowercase()}"),
        colors = ButtonDefaults.buttonColors(
          containerColor = CyberSurfaceVariant,
          disabledContainerColor = CyberSurface.copy(alpha = 0.5f),
          contentColor = NeonCyan,
          disabledContentColor = TextMuted
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (canAfford) NeonCyan.copy(alpha = 0.8f) else CyberBorder
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = icon,
            contentDescription = ability.name,
            tint = if (canAfford) NeonCyan else TextMuted,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = ability.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(4.dp))
          Surface(
            color = if (canAfford) CyberEnergy.copy(alpha = 0.2f) else Color.Transparent,
            shape = RoundedCornerShape(3.dp)
          ) {
            Text(
              text = "${ability.energyCost}E",
              color = if (canAfford) CyberEnergy else TextMuted,
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
            )
          }
        }
      }
    }
  }
}
