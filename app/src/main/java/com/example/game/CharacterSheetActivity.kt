package com.example.game

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class CharacterSheetActivity : ComponentActivity() {
    private lateinit var gameData: GameData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameData = GameData(this)
        setContent {
            MaterialTheme {
                CharacterSheetScreen()
            }
        }
    }

    @Composable
    fun CharacterSheetScreen() {
        var playerStats by remember { mutableStateOf(gameData.getPlayerStats()) }
        var isRefreshing by remember { mutableStateOf(false) }

        // Обновляем статистику каждые 1000ms
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                playerStats = gameData.getPlayerStats()
            }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Фон персонажа
                Image(
                    painter = painterResource(id = R.drawable.personazh),
                    contentDescription = "Character Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Затемнение
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Заголовок
                    Text(
                        text = "Лист персонажа",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDD835),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Уровень и общая информация
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Уровень ${playerStats.level}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDD835)
                            )

                            Text(
                                text = "Здоровье: ${playerStats.hp}/100",
                                fontSize = 16.sp,
                                color = if (playerStats.hp > 50) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )

                            Text(
                                text = "Карты опыта: ${playerStats.cards}",
                                fontSize = 16.sp,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }

                    // Карточка с основными характеристиками
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Удача
                            StatRow(
                                name = "Удача",
                                value = playerStats.luck,
                                icon = "⚡",
                                color = Color(0xFFFFC107),
                                description = "Влияет на случайные события"
                            )

                            // Разум
                            StatRow(
                                name = "Разум",
                                value = playerStats.mind,
                                icon = "🧠",
                                color = Color(0xFF2196F3),
                                description = "Необходим для загадок и магии"
                            )

                            // Сила
                            StatRow(
                                name = "Сила",
                                value = playerStats.power,
                                icon = "💪",
                                color = Color(0xFFF44336),
                                description = "Определяет успех в бою"
                            )
                        }
                    }

                    // Инвентарь
                    if (playerStats.inventory.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "🎒 Инвентарь",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDD835),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                playerStats.inventory.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Кнопки
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Кнопка возврата в геймплей
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

                        // Кнопка обновления
                        Button(
                            onClick = {
                                isRefreshing = true
                                playerStats = gameData.getPlayerStats()
                                isRefreshing = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White
                            ),
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Обновить данные",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Кнопка назад в меню
                        Button(
                            onClick = { navigateToMain() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF757575),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Главное меню",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun StatRow(
        name: String,
        value: Int,
        icon: String,
        color: Color,
        description: String
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = icon,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Text(
                    text = value.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 36.dp, top = 4.dp)
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
}
