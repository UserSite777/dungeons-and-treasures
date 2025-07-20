package com.example.game

import android.content.Context
import android.content.SharedPreferences
import java.time.Instant
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
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("game_data", Context.MODE_PRIVATE)

    fun getPlayerStats(): PlayerStats {
        return PlayerStats(
            luck = sharedPreferences.getInt("luck", 3),
            mind = sharedPreferences.getInt("mind", 3),
            power = sharedPreferences.getInt("power", 3),
            hp = sharedPreferences.getInt("hp", 100),
            cards = sharedPreferences.getInt("cards", 0),
            level = sharedPreferences.getInt("level", 1),
            inventory = sharedPreferences.getStringSet("inventory", null)?.toList() ?: emptyList()
        )
    }

    fun savePlayerStats(stats: PlayerStats) {
        sharedPreferences.edit()
            .putInt("luck", stats.luck)
            .putInt("mind", stats.mind)
            .putInt("power", stats.power)
            .putInt("hp", stats.hp)
            .putInt("cards", stats.cards)
            .putInt("level", stats.level)
            .putStringSet("inventory", stats.inventory.toSet())
            .apply()
    }

    // Добавить зелье или другой предмет
    fun addToInventory(item: String) {
        val inv = getPlayerStats().inventory.toMutableList()
        inv.add(item)
        sharedPreferences.edit().putStringSet("inventory", inv.toSet()).apply()
    }

    fun setInventory(list: List<String>) {
        sharedPreferences.edit().putStringSet("inventory", list.toSet()).apply()
    }

    // Получить имя выбранного спутника
    fun getCurrentCompanionName(): String =
        sharedPreferences.getString("selected_character", "") ?: ""

    fun getSelectedCharacter(): CharacterSheetActivity.CompanionCharacter? {
        val name = sharedPreferences.getString("selected_character", null)
        return try {
            CharacterSheetActivity.CompanionCharacter.valueOf(name ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun setSelectedCharacter(character: CharacterSheetActivity.CompanionCharacter?) {
        sharedPreferences.edit().apply {
            putString("selected_character", character?.name)
            apply()
        }
    }

    fun setCooldownUntil(time: Instant) {
        sharedPreferences.edit().apply {
            putLong("cooldown_until", time.toEpochMilli())
            apply()
        }
    }

    fun getCooldownUntil(): Instant {
        val epochMillis = sharedPreferences.getLong("cooldown_until", 0)
        return if (epochMillis > 0) Instant.ofEpochMilli(epochMillis) else Instant.now().minusSeconds(600)
    }

    // Сброс всех данных
    fun resetGameData() {
        sharedPreferences.edit().clear().apply()
    }
}
