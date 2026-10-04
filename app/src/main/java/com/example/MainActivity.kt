package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.PrankDestination
import com.example.ui.screens.MainPrankApp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val data = intent?.data
        val isPrankTrap = data?.getQueryParameter("prank") == "trap"
        val durationMins = data?.getQueryParameter("duration")?.toIntOrNull() ?: 10
        val disguiseCode = data?.getQueryParameter("disguise") ?: "gift"

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MainPrankApp(
                        initialDestination = if (isPrankTrap) PrankDestination.PRANK_LINK else PrankDestination.SOUNDBOARD,
                        autoStartTrap = isPrankTrap,
                        trapDurationMinutes = durationMins,
                        trapDisguiseCode = disguiseCode
                    )
                }
            }
        }
    }
}
