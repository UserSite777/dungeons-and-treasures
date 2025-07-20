package com.example.game

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
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
        // Скрываем системные кнопки Android
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
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    GameplayScreen()
                }
            }
        }
    }

    @Composable
    fun GameplayScreen() {
        var playerStats by remember { mutableStateOf(gameData.getPlayerStats()) }
        var isLoading by remember { mutableStateOf(true) }

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
            // Хедер - для интерфейса игры
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                GameHeader(hp = playerStats.hp, cards = playerStats.cards, isLoading = isLoading)
            }

            // WebView с игрой
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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

            // Футер с кнопками
            GameFooter()
        }
    }

    @Composable
    fun GameHeader(hp: Int, cards: Int, isLoading: Boolean) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFF2C2C2C)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                StatCard(
                    label = "Карты (XP)",
                    value = cards,
                    icon = "🎯",
                    color = Color(0xFF2196F3),
                    isLoading = isLoading
                )
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { navigateToCharacterSheet() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                )
            ) {
                Text("Лист персонажа", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    if (::webView.isInitialized) {
                        webView.evaluateJavascript("if(window.gameInterface) window.gameInterface.pauseGame();", null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text("Сохранить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { navigateToMain() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF757575),
                    contentColor = Color.White
                )
            ) {
                Text("Выход", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
        super.onBackPressed()
    }

    // ✅ Новый метод для JS
    fun navigateToMainFromJS() {
        navigateToMain()
    }
}
