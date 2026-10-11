package com.example.data

import android.content.Context
import android.content.SharedPreferences

class MetaProgressionRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("neon_crush_meta_prefs", Context.MODE_PRIVATE)

  fun getNeonShards(): Int = prefs.getInt("neon_shards", 25)

  fun addNeonShards(amount: Int) {
    val current = getNeonShards()
    prefs.edit().putInt("neon_shards", (current + amount).coerceAtLeast(0)).apply()
  }

  fun spendNeonShards(amount: Int): Boolean {
    val current = getNeonShards()
    if (current >= amount) {
      prefs.edit().putInt("neon_shards", current - amount).apply()
      return true
    }
    return false
  }

  fun getUpgradeLevel(upgradeId: String): Int = prefs.getInt("upgrade_$upgradeId", 0)

  fun incrementUpgradeLevel(upgradeId: String) {
    val current = getUpgradeLevel(upgradeId)
    prefs.edit().putInt("upgrade_$upgradeId", current + 1).apply()
  }

  fun getHighScore(): Int = prefs.getInt("high_score", 0)

  fun recordScore(score: Int) {
    val high = getHighScore()
    if (score > high) {
      prefs.edit().putInt("high_score", score).apply()
    }
  }

  fun getRunsCompleted(): Int = prefs.getInt("runs_completed", 0)

  fun incrementRunsCompleted() {
    prefs.edit().putInt("runs_completed", getRunsCompleted() + 1).apply()
  }
}
