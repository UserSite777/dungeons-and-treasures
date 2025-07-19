package com.example.game

import android.content.Context
import android.content.SharedPreferences

data class PlayerStats(
    val luck: Int = 3,
    val mind: Int = 3,
    val power: Int = 3,
    val hp: Int = 100,
    val cards: Int = 0,
    val level: Int = 1,
    val inventory: List<String> = emptyList()
)

class GameData(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_data", Context.MODE_PRIVATE)

    fun getPlayerStats(): PlayerStats {
        return PlayerStats(
            luck = sharedPreferences.getInt("luck", 3),
            mind = sharedPreferences.getInt("mind", 3),
            power = sharedPreferences.getInt("power", 3),
            hp = sharedPreferences.getInt("hp", 100),
            cards = sharedPreferences.getInt("cards", 0),
            level = sharedPreferences.getInt("level", 1),
            inventory = sharedPreferences.getStringSet("inventory", emptySet())?.toList() ?: emptyList()
        )
    }

    fun savePlayerStats(stats: PlayerStats) {
        sharedPreferences.edit().apply {
            putInt("luck", stats.luck)
            putInt("mind", stats.mind)
            putInt("power", stats.power)
            putInt("hp", stats.hp)
            putInt("cards", stats.cards)
            putInt("level", stats.level)
            putStringSet("inventory", stats.inventory.toSet())
            apply()
        }
    }

    fun resetGameData() {
        sharedPreferences.edit().clear().apply()
    }

    fun updateStat(statName: String, value: Int) {
        sharedPreferences.edit().apply {
            putInt(statName, value)
            apply()
        }
    }

    fun getStat(statName: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(statName, defaultValue)
    }

    fun addToInventory(item: String) {
        val currentStats = getPlayerStats()
        val newInventory = currentStats.inventory.toMutableList()
        if (!newInventory.contains(item)) {
            newInventory.add(item)
            savePlayerStats(currentStats.copy(inventory = newInventory))
        }
    }

    fun removeFromInventory(item: String) {
        val currentStats = getPlayerStats()
        val newInventory = currentStats.inventory.toMutableList()
        newInventory.remove(item)
        savePlayerStats(currentStats.copy(inventory = newInventory))
    }
}
