package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.RelicRarity
import com.example.ui.components.CyberPlayerHud
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState
import com.example.viewmodel.ShopItem

@Composable
fun ShopScreen(
  uiState: GameUiState,
  onBuyRelic: (ShopItem) -> Unit,
  onBuyHeal: () -> Unit,
  onLeaveShop: () -> Unit,
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

      Spacer(modifier = Modifier.height(12.dp))

      // Header Banner
      Surface(
        color = CyberSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Storefront, contentDescription = "Shop", tint = CyberGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(text = "BLACK MARKET VENDOR", color = CyberGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              Text(text = "Enhance your matrix capabilities", color = TextMuted, fontSize = 11.sp)
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MonetizationOn, contentDescription = "Credits", tint = CyberGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${uiState.player.credits}",
              color = CyberGold,
              fontSize = 15.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Relics and Repair Items List
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.heightIn(max = 420.dp)
      ) {
        // Nanite Repair Item
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Nanite Repair Injection", color = CyberHealth, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = "Restore +35 Hull HP integrity", color = TextSecondary, fontSize = 11.sp)
              }
              Button(
                onClick = onBuyHeal,
                enabled = uiState.player.credits >= 30 && uiState.player.hullHp < uiState.player.maxHullHp,
                colors = ButtonDefaults.buttonColors(containerColor = CyberHealth),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("buy_heal_button")
              ) {
                Text(text = "30 CR", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
          }
        }

        // Relics for Sale
        items(uiState.shopRelics) { shopItem ->
          val relic = shopItem.relic
          val canAfford = uiState.player.credits >= relic.costCredits && !shopItem.isPurchased

          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (shopItem.isPurchased) CyberSurface.copy(alpha = 0.5f) else CyberSurfaceVariant),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              when (relic.rarity) {
                RelicRarity.LEGENDARY -> NeonAmber
                RelicRarity.RARE -> NeonMagenta
                RelicRarity.COMMON -> CyberBorder
              }
            )
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
                  Text(text = relic.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    color = when (relic.rarity) {
                      RelicRarity.LEGENDARY -> NeonAmber.copy(alpha = 0.2f)
                      RelicRarity.RARE -> NeonMagenta.copy(alpha = 0.2f)
                      RelicRarity.COMMON -> NeonCyan.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = relic.rarity.label,
                      color = when (relic.rarity) {
                        RelicRarity.LEGENDARY -> NeonAmber
                        RelicRarity.RARE -> NeonMagenta
                        RelicRarity.COMMON -> NeonCyan
                      },
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = relic.description, color = TextSecondary, fontSize = 11.sp)
              }

              Button(
                onClick = { onBuyRelic(shopItem) },
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                  containerColor = CyberGold,
                  contentColor = Color.Black,
                  disabledContainerColor = CyberSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("buy_${relic.id}")
              ) {
                Text(
                  text = if (shopItem.isPurchased) "OWNED" else "${relic.costCredits} CR",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }

    Button(
      onClick = onLeaveShop,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("leave_shop_button"),
      colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
      shape = RoundedCornerShape(10.dp)
    ) {
      Text(
        text = "PROCEED TO SECTOR MATRIX",
        color = NeonCyan,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
