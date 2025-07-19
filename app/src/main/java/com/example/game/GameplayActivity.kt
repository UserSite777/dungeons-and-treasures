package com.example.game

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

class GameplayActivity : ComponentActivity() {
    private lateinit var gameData: GameData
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameData = GameData(this)
        setContent {
            MaterialTheme {
                GameplayScreen()
            }
        }
    }

    @Composable
    fun GameplayScreen() {
        var playerStats by remember { mutableStateOf(gameData.getPlayerStats()) }
        var isLoading by remember { mutableStateOf(true) }

        // Обновляем статистику каждые 500ms
        LaunchedEffect(Unit) {
            while (true) {
                delay(500)
                playerStats = gameData.getPlayerStats()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Хедер с основными характеристиками
            GameHeader(
                hp = playerStats.hp,
                cards = playerStats.cards,
                isLoading = isLoading
            )

            // WebView с игрой
            Box(
                modifier = Modifier.weight(1f)
            ) {
                GameWebView(
                    onStatsChanged = {
                        playerStats = gameData.getPlayerStats()
                    },
                    onLoadingChanged = { loading ->
                        isLoading = loading
                    }
                )
            }

            // Футер с кнопками навигации
            GameFooter()
        }
    }

    @Composable
    fun GameHeader(hp: Int, cards: Int, isLoading: Boolean) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2C2C2C)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Здоровье
                StatCard(
                    label = "Здоровье",
                    value = hp,
                    icon = "❤️",
                    color = when {
                        hp > 70 -> Color(0xFF4CAF50)
                        hp > 30 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    isLoading = isLoading
                )

                // Карты (XP)
                StatCard(
                    label = "Карты (XP)",
                    value = cards,
                    icon = "🎯",
                    color = Color(0xFF2196F3),
                    isLoading = isLoading
                )

                // Индикатор загрузки
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFF4CAF50).copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun StatCard(
        label: String,
        value: Int,
        icon: String,
        color: Color,
        isLoading: Boolean = false
    ) {
        Card(
            modifier = Modifier.padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = icon,
                    fontSize = 16.sp
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = color,
                        strokeWidth = 1.dp
                    )
                } else {
                    Text(
                        text = value.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun GameWebView(
        onStatsChanged: () -> Unit,
        onLoadingChanged: (Boolean) -> Unit
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            onLoadingChanged(true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            onLoadingChanged(false)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            error: android.webkit.WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            onLoadingChanged(false)
                        }
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        defaultTextEncodingName = "utf-8"
                        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    }

                    setBackgroundColor(android.graphics.Color.BLACK)

                    addJavascriptInterface(
                        WebAppInterface(context) {
                            onStatsChanged()
                        },
                        "android"
                    )

                    // ✅ Загружаем файл из res/raw/
                    loadUrl("file:///android_res/raw/index.html")
                }
            },
            update = { view ->
                // Обновляем WebView при необходимости
            }
        )
    }


    @Composable
    fun GameFooter() {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2C2C2C)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Кнопка листа персонажа
                Button(
                    onClick = { navigateToCharacterSheet() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Лист персонажа", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопка сохранения
                Button(
                    onClick = {
                        if (::webView.isInitialized) {
                            webView.evaluateJavascript("if(window.gameInterface) window.gameInterface.pauseGame();", null)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Сохранить", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопка выхода в меню
                Button(
                    onClick = { navigateToMain() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF757575)
                    )
                ) {
                    Text("Выход", fontSize = 12.sp)
                }
            }
        }
    }

    private fun navigateToCharacterSheet() {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("if(window.gameInterface) window.gameInterface.pauseGame();", null)
        }
        startActivity(Intent(this, CharacterSheetActivity::class.java))
    }

    private fun navigateToMain() {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("if(window.gameInterface) window.gameInterface.pauseGame();", null)
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onPause() {
        super.onPause()
        if (::webView.isInitialized) {
            webView.evaluateJavascript("if(window.gameInterface) window.gameInterface.pauseGame();", null)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.evaluateJavascript("if(window.gameInterface) window.gameInterface.resumeGame();", null)
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            navigateToMain()
        }
        super.onBackPressed() // ✅ ВАЖНО: вызов super.onBackPressed()
    }
}
