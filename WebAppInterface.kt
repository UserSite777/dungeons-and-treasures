package com.example.game

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONObject

class WebAppInterface(
    private val context: Context,
    private val onStatsChanged: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private val gameData = GameData(context)

    @JavascriptInterface
    fun updatePlayerStats(
        luck: Int,
        mind: Int,
        power: Int,
        hp: Int,
        cards: Int,
        level: Int,
        inventoryJson: String = "[]"
    ) {
        handler.post {
            try {
                val receivedLuck = luck
                val receivedMind = mind
                val receivedPower = power
                val receivedHp = hp
                val receivedCards = cards
                val receivedLevel = level

                val currentStats = gameData.getPlayerStats()
                val newStats = currentStats.copy(
                    luck = receivedLuck,
                    mind = receivedMind,
                    power = receivedPower,
                    hp = receivedHp,
                    cards = receivedCards,
                    level = receivedLevel,
                    inventory = parseInventory(inventoryJson)
                )
                gameData.savePlayerStats(newStats)
                onStatsChanged()
            } catch (e: Exception) {
                showToast("Ошибка обновления статистики: ${e.message}")
            }
        }
    }

    @JavascriptInterface
    fun getPlayerStats(): String {
        return try {
            val stats = gameData.getPlayerStats()
            JSONObject().apply {
                put("luck", stats.luck)
                put("mind", stats.mind)
                put("power", stats.power)
                put("hp", stats.hp)
                put("cards", stats.cards)
                put("level", stats.level)
                put("inventory", stats.inventory.joinToString(","))
            }.toString()
        } catch (e: Exception) {
            "{\"luck\":3,\"mind\":3,\"power\":3,\"hp\":100,\"cards\":0,\"level\":1,\"inventory\":\"\"}"
        }
    }

    @JavascriptInterface
    fun saveGameProgress(sceneId: String, playerData: String) {
        handler.post {
            try {
                val sharedPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                sharedPrefs.edit().apply {
                    putString("current_scene", sceneId)
                    putString("player_data", playerData)
                    apply()
                }
            } catch (e: Exception) {
                showToast("Ошибка сохранения: ${e.message}")
            }
        }
    }

    @JavascriptInterface
    fun loadGameProgress(): String {
        return try {
            val sharedPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
            val currentScene = sharedPrefs.getString("current_scene", "intro") ?: "intro"
            val playerData = sharedPrefs.getString("player_data", "{}") ?: "{}"

            JSONObject().apply {
                put("scene", currentScene)
                put("player", playerData)
            }.toString()
        } catch (e: Exception) {
            "{\"scene\":\"intro\",\"player\":\"{}\"}"
        }
    }

    @JavascriptInterface
    fun resetGame() {
        handler.post {
            gameData.resetGameData()
            val sharedPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
            onStatsChanged()
            showToast("Игра сброшена")
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        handler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun log(message: String) {
        handler.post {
            android.util.Log.d("GameWebView", message)
        }
    }

    // Вспомогательная функция для парсинга инвентаря
    private fun parseInventory(inventoryJson: String): List<String> {
        return try {
            if (inventoryJson.isEmpty() || inventoryJson == "[]") {
                emptyList()
            } else {
                inventoryJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
