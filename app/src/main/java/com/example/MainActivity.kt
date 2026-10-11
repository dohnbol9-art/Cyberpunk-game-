package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg)
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .safeDrawingPadding()
          ) {
            NeonCrushApp()
          }
        }
      }
    }
  }
}

@Composable
fun NeonCrushApp() {
  val context = androidx.compose.ui.platform.LocalContext.current
  val viewModel: GameViewModel = viewModel { GameViewModel(context.applicationContext) }
  val uiState by viewModel.uiState.collectAsState()

  // Handle system back navigation across screens
  when (uiState.currentScreen) {
    GameScreen.MAIN_MENU -> {
      // System default back exits app
    }
    GameScreen.SECTOR_MAP -> {
      BackHandler {
        viewModel.navigateTo(GameScreen.MAIN_MENU)
      }
    }
    GameScreen.BATTLE -> {
      BackHandler {
        // Return to sector map if needed
        viewModel.navigateTo(GameScreen.SECTOR_MAP)
      }
    }
    GameScreen.SHOP, GameScreen.EVENT, GameScreen.REST -> {
      BackHandler {
        viewModel.finishNonCombatNode()
      }
    }
    GameScreen.CITADEL -> {
      BackHandler {
        viewModel.navigateTo(GameScreen.MAIN_MENU)
      }
    }
    GameScreen.VICTORY, GameScreen.GAME_OVER -> {
      BackHandler {
        viewModel.navigateTo(GameScreen.MAIN_MENU)
      }
    }
  }

  when (uiState.currentScreen) {
    GameScreen.MAIN_MENU -> {
      MainMenuScreen(
        uiState = uiState,
        onSelectClass = { viewModel.selectHeroClass(it) },
        onStartRun = { viewModel.startNewRun() },
        onOpenCitadel = { viewModel.navigateTo(GameScreen.CITADEL) }
      )
    }
    GameScreen.SECTOR_MAP -> {
      SectorMapScreen(
        uiState = uiState,
        onSelectNode = { viewModel.onSelectMapNode(it) }
      )
    }
    GameScreen.BATTLE -> {
      BattleScreen(
        uiState = uiState,
        particleManager = viewModel.particleManager,
        onGemClick = { r, c, cellSizePx, boardOrigin ->
          viewModel.onGemClick(r, c, cellSizePx, boardOrigin)
        },
        onActivateAbility = { viewModel.activateAbility(it) },
        onCancelTargeting = {
          viewModel.onGemClick(-1, -1, 0f, androidx.compose.ui.geometry.Offset.Zero)
        }
      )
    }
    GameScreen.SHOP -> {
      ShopScreen(
        uiState = uiState,
        onBuyRelic = { viewModel.buyShopRelic(it) },
        onBuyHeal = { viewModel.buyShopHeal() },
        onLeaveShop = { viewModel.finishNonCombatNode() }
      )
    }
    GameScreen.EVENT -> {
      EventScreen(
        uiState = uiState,
        onSelectOption = { viewModel.resolveEventOption(it) }
      )
    }
    GameScreen.REST -> {
      RestScreen(
        uiState = uiState,
        onRestHeal = { viewModel.restHeal() },
        onRestOverclock = { viewModel.restOverclock() }
      )
    }
    GameScreen.CITADEL -> {
      CitadelScreen(
        uiState = uiState,
        onBuyUpgrade = { viewModel.purchaseCitadelUpgrade(it) },
        onBack = { viewModel.navigateTo(GameScreen.MAIN_MENU) }
      )
    }
    GameScreen.VICTORY -> {
      EndRunScreen(
        uiState = uiState,
        isVictory = true,
        onReturnHome = { viewModel.navigateTo(GameScreen.MAIN_MENU) }
      )
    }
    GameScreen.GAME_OVER -> {
      EndRunScreen(
        uiState = uiState,
        isVictory = false,
        onReturnHome = { viewModel.navigateTo(GameScreen.MAIN_MENU) }
      )
    }
  }
}
