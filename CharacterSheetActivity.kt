package com.example.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.temporal.ChronoUnit

class CharacterSheetActivity : ComponentActivity() {
    private lateinit var gameData: GameData

    override fun onCreate(savedInstanceState: Bundle?) {
        // ❗️Важно: вызов requestWindowFeature() должен быть до super.onCreate()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        super.onCreate(savedInstanceState)
        gameData = GameData(this)

        val initialPlayerStats = gameData.getPlayerStats()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    CharacterSheetScreen(initialPlayerStats)
                }
            }
        }
    }

    @Composable
    fun CharacterSheetScreen(initialPlayerStats: PlayerStats) {
        var playerStats by remember { mutableStateOf(initialPlayerStats) }
        var selectedCharacter by remember { mutableStateOf<CharacterSheetActivity.CompanionCharacter?>(gameData.getSelectedCharacter()) }
        var cooldownUntil by remember { mutableStateOf(gameData.getCooldownUntil()) }
        val now = Instant.now()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Основной контент
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Хедер с рисунком
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.personazh),
                        contentDescription = "Character Background",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                }

                // Заголовок
                Text(
                    text = "Лист персонажа",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDD835),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Параметры
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("⚡ Удача", playerStats.luck, Color(0xFFFFC107))
                    StatCard("🧠 Разум", playerStats.mind, Color(0xFF2196F3))
                    StatCard("💪 Сила", playerStats.power, Color(0xFFF44336))
                }

                // Инвентарь
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val zelyaCount = playerStats.inventory.count { it == "Зелье здоровья" }
                            Text(
                                text = "🎒 Зелье здоровья x$zelyaCount",
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp),
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                // Слайдер персонажей с чекбоксами
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CharacterWithCheckbox(
                            title = "🧙‍♂️ Маг",
                            imageRes = R.drawable.mage,
                            description = "Маг усиливает Разум (+3)",
                            selected = selectedCharacter == CharacterSheetActivity.CompanionCharacter.MAGE,
                            onSelected = { isChecked ->
                                if (isChecked) {
                                    if (selectedCharacter != null || cooldownUntil.isAfter(now)) return@CharacterWithCheckbox
                                    playerStats = playerStats.copy(mind = playerStats.mind + 3)
                                    selectedCharacter = CharacterSheetActivity.CompanionCharacter.MAGE
                                    cooldownUntil = now.plus(10, ChronoUnit.MINUTES)
                                } else {
                                    playerStats = playerStats.copy(mind = maxOf(playerStats.mind - 3, 0))
                                    selectedCharacter = null
                                }
                                gameData.savePlayerStats(playerStats)
                                gameData.setSelectedCharacter(selectedCharacter)
                                gameData.setCooldownUntil(cooldownUntil)
                            },
                            cooldownUntil = cooldownUntil,
                            now = now,
                            character = CharacterSheetActivity.CompanionCharacter.MAGE
                        )
                        CharacterWithCheckbox(
                            title = "🏹 Лучник",
                            imageRes = R.drawable.archer,
                            description = "Лучник усиливает Удачу (+3)",
                            selected = selectedCharacter == CharacterSheetActivity.CompanionCharacter.ARCHER,
                            onSelected = { isChecked ->
                                if (isChecked) {
                                    if (selectedCharacter != null || cooldownUntil.isAfter(now)) return@CharacterWithCheckbox
                                    playerStats = playerStats.copy(luck = playerStats.luck + 3)
                                    selectedCharacter = CharacterSheetActivity.CompanionCharacter.ARCHER
                                    cooldownUntil = now.plus(10, ChronoUnit.MINUTES)
                                } else {
                                    playerStats = playerStats.copy(luck = maxOf(playerStats.luck - 3, 0))
                                    selectedCharacter = null
                                }
                                gameData.savePlayerStats(playerStats)
                                gameData.setSelectedCharacter(selectedCharacter)
                                gameData.setCooldownUntil(cooldownUntil)
                            },
                            cooldownUntil = cooldownUntil,
                            now = now,
                            character = CharacterSheetActivity.CompanionCharacter.ARCHER
                        )
                        CharacterWithCheckbox(
                            title = "⚔️ Воин",
                            imageRes = R.drawable.warrior,
                            description = "Воин усиливает Силу (+3)",
                            selected = selectedCharacter == CharacterSheetActivity.CompanionCharacter.WARRIOR,
                            onSelected = { isChecked ->
                                if (isChecked) {
                                    if (selectedCharacter != null || cooldownUntil.isAfter(now)) return@CharacterWithCheckbox
                                    playerStats = playerStats.copy(power = playerStats.power + 3)
                                    selectedCharacter = CharacterSheetActivity.CompanionCharacter.WARRIOR
                                    cooldownUntil = now.plus(10, ChronoUnit.MINUTES)
                                } else {
                                    playerStats = playerStats.copy(power = maxOf(playerStats.power - 3, 0))
                                    selectedCharacter = null
                                }
                                gameData.savePlayerStats(playerStats)
                                gameData.setSelectedCharacter(selectedCharacter)
                                gameData.setCooldownUntil(cooldownUntil)
                            },
                            cooldownUntil = cooldownUntil,
                            now = now,
                            character = CharacterSheetActivity.CompanionCharacter.WARRIOR
                        )
                    }

                    // Отображение выбранного бафа и кулдауна
                    if (selectedCharacter != null) {
                        Text(
                            text = when (selectedCharacter) {
                                CharacterSheetActivity.CompanionCharacter.MAGE -> "Выбран Маг: +3 к Разуму"
                                CharacterSheetActivity.CompanionCharacter.ARCHER -> "Выбран Лучник: +3 к Удаче"
                                CharacterSheetActivity.CompanionCharacter.WARRIOR -> "Выбран Воин: +3 к Силе"
                                else -> ""
                            },
                            fontSize = 16.sp,
                            color = Color(0xFFE6EE9C),
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    if (cooldownUntil.isAfter(now)) {
                        Text(
                            text = "Смена спутника доступна через ${formatCooldown(cooldownUntil)}",
                            fontSize = 14.sp,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Кнопки внизу экрана
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navigateToGameplay() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Вернуться в игру",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { navigateToMain() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF757575),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Главное меню",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    @Composable
    fun StatCard(name: String, value: Int, color: Color) {
        Card(
            modifier = Modifier
                .width(100.dp)
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }

    @Composable
    fun CharacterWithCheckbox(
        title: String,
        imageRes: Int,
        description: String,
        selected: Boolean,
        onSelected: (Boolean) -> Unit,
        cooldownUntil: Instant,
        now: Instant,
        character: CharacterSheetActivity.CompanionCharacter
    ) {
        val isOnCooldown = cooldownUntil.isAfter(now)
        val canSelect = selected || !isOnCooldown

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFFE6EE9C),
                modifier = Modifier.padding(top = 4.dp)
            )
            Checkbox(
                checked = selected,
                onCheckedChange = if (canSelect) onSelected else null,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    private fun navigateToGameplay() {
        startActivity(Intent(this, GameplayActivity::class.java))
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun formatCooldown(cooldownUntil: Instant): String {
        val duration = java.time.Duration.between(Instant.now(), cooldownUntil)
        val minutes = duration.toMinutes()
        val seconds = duration.seconds % 60
        return "$minutes мин $seconds сек"
    }

    enum class CompanionCharacter {
        MAGE, ARCHER, WARRIOR
    }
}
