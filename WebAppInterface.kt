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
        inventoryJson: String
    ) {
        handler.post {
            try {
                val inventory = parseInventory(inventoryJson)
                val stats = PlayerStats(luck, mind, power, hp, cards, level, inventory)
                gameData.savePlayerStats(stats)
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
            val obj = JSONObject()
            obj.put("luck", stats.luck)
            obj.put("mind", stats.mind)
            obj.put("power", stats.power)
            obj.put("hp", stats.hp)
            obj.put("cards", stats.cards)
            obj.put("level", stats.level)
            obj.put("inventory", stats.inventory.joinToString(","))
            obj.put("companion", gameData.getCurrentCompanionName())
            obj.toString()
        } catch (e: Exception) {
            "{\"luck\":3,\"mind\":3,\"power\":3,\"hp\":100,\"cards\":0,\"level\":1,\"inventory\":\"\",\"companion\":\"\"}"
        }
    }

    @JavascriptInterface
    fun addToInventory(item: String) {
        handler.post {
            gameData.addToInventory(item)
            onStatsChanged()
        }
    }

    @JavascriptInterface
    fun getCurrentCompanion(): String {
        return gameData.getCurrentCompanionName()
    }

    @JavascriptInterface
    fun saveGameProgress(sceneId: String, playerData: String) {
        handler.post {
            val sharedPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putString("current_scene", sceneId)
                .putString("player_data", playerData)
                .apply()
        }
    }

    @JavascriptInterface
    fun loadGameProgress(): String {
        val sharedPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
        val obj = JSONObject()
        obj.put("scene", sharedPrefs.getString("current_scene", "intro"))
        obj.put("player", sharedPrefs.getString("player_data", "{}"))
        return obj.toString()
    }

    @JavascriptInterface
    fun resetGame() {
        handler.post {
            gameData.resetGameData()
            val prefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            onStatsChanged()
            showToast("Игра сброшена")
        }
    }

    @JavascriptInterface
    fun goBackToMainMenu() {
        handler.post {
            if (context is GameplayActivity) {
                context.navigateToMainFromJS()
            }
        }
    }

    @JavascriptInterface
    fun syncStatsToJS(): String {
        val stats = gameData.getPlayerStats()
        val json = JSONObject().apply {
            put("luck", stats.luck)
            put("mind", stats.mind)
            put("power", stats.power)
            put("hp", stats.hp)
            put("cards", stats.cards)
            put("level", stats.level)
            put("inventory", stats.inventory.joinToString(","))
            put("companion", gameData.getCurrentCompanionName())
        }
        return json.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun parseInventory(json: String): List<String> {
        if (json.trim().isEmpty() || json.trim() == "[]") return emptyList()
        return json.removePrefix("[").removeSuffix("]").split(",").map { it.trim('"', ' ', ',') }.filter { it.isNotEmpty() }
    }
}
