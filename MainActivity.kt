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

class MainActivity : ComponentActivity() {
    private lateinit var gameData: GameData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameData = GameData(this)

        setContent {
            MaterialTheme {
                MainMenuScreen()
            }
        }
    }

    @Composable
    fun MainMenuScreen() {
        var showTermsDialog by remember { mutableStateOf(false) }
        var showResetDialog by remember { mutableStateOf(false) }

        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Фон замка
                Image(
                    painter = painterResource(id = R.drawable.zamok),
                    contentDescription = "Castle Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Затемнение для лучшей читаемости
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                // Основной контент
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Заголовок
                    Text(
                        text = "Подземелья и сокровища",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDD835),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )

                    // Кнопка "Начать игру"
                    GameButton(
                        text = "Начать игру",
                        color = Color(0xFF4CAF50),
                        onClick = { showTermsDialog = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка "Лист персонажа"
                    GameButton(
                        text = "Лист персонажа",
                        color = Color(0xFF2196F3),
                        onClick = { navigateToCharacterSheet() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка "Сбросить прогресс"
                    GameButton(
                        text = "Сбросить прогресс",
                        color = Color(0xFFFF9800),
                        onClick = { showResetDialog = true }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка "Выход"
                    GameButton(
                        text = "Выход",
                        color = Color(0xFFF44336),
                        onClick = { exitApp() }
                    )
                }
            }
        }

        // Диалог условий
        if (showTermsDialog) {
            TermsDialog(
                onAccept = {
                    showTermsDialog = false
                    navigateToGameplay()
                },
                onDismiss = { showTermsDialog = false }
            )
        }

        // Диалог сброса прогресса
        if (showResetDialog) {
            ResetProgressDialog(
                onConfirm = {
                    resetGameProgress()
                    showResetDialog = false
                },
                onDismiss = { showResetDialog = false }
            )
        }
    }

    @Composable
    fun GameButton(text: String, color: Color, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Color.White
            )
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    @Composable
    fun TermsDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Условия использования") },
            text = {
                Text(
                    "Добро пожаловать в Подземелья и сокровища! " +
                            "Готовы ли вы к опасностям Проклятых земель? " +
                            "Ваши решения будут определять судьбу персонажа. " +
                            "Удачи в поисках Грааля!"
                )
            },
            confirmButton = {
                Button(onClick = onAccept) {
                    Text("Принять и начать")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
    }

    @Composable
    fun ResetProgressDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Сброс прогресса") },
            text = {
                Text(
                    "Вы уверены, что хотите сбросить весь прогресс? " +
                            "Это действие удалит все сохранения, статистику персонажа, " +
                            "выбранных спутников и прогресс в игре. " +
                            "Отменить это действие будет невозможно!"
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    )
                ) {
                    Text("Да, сбросить")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
    }

    private fun navigateToGameplay() {
        startActivity(Intent(this, GameplayActivity::class.java))
    }

    private fun navigateToCharacterSheet() {
        startActivity(Intent(this, CharacterSheetActivity::class.java))
    }

    private fun resetGameProgress() {
        try {
            // Сбрасываем все данные игры через GameData
            gameData.resetGameData()

            // Очищаем прогресс игры
            val gameProgressPrefs = getSharedPreferences("game_progress", MODE_PRIVATE)
            gameProgressPrefs.edit().clear().apply()

            // Показываем уведомление
            android.widget.Toast.makeText(this, "Прогресс игры полностью сброшен", android.widget.Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Ошибка при сбросе прогресса: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun exitApp() {
        finishAffinity()
        System.exit(0)
    }
}
