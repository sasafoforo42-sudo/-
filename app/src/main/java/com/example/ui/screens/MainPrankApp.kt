package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.PrankAudioEngine
import com.example.hardware.FlashlightController
import com.example.hardware.PrankHaptics
import com.example.ui.navigation.PrankDestination
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPrankApp(
    initialDestination: PrankDestination = PrankDestination.SOUNDBOARD,
    autoStartTrap: Boolean = false,
    trapDurationMinutes: Int = 10,
    trapDisguiseCode: String = "gift"
) {
    val context = LocalContext.current
    val audioEngine = remember { PrankAudioEngine() }
    val haptics = remember { PrankHaptics(context) }
    val flashlight = remember { FlashlightController(context) }

    var currentDestination by remember { mutableStateOf(initialDestination) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var isTrapActiveOnLaunch by remember { mutableStateOf(autoStartTrap) }

    // Handle back button to return to home soundboard if in a subscreen
    BackHandler(enabled = currentDestination != PrankDestination.SOUNDBOARD) {
        currentDestination = PrankDestination.SOUNDBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .background(DarkBackground)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = NeonYellow.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.5f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🤡", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PRANK",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "MASTER",
                                    color = NeonYellow,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Шутки, звуки и розыгрыши",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = {
                                currentDestination = PrankDestination.PRANK_LINK
                                haptics.vibrateShort(25)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = NeonOrange.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🔗", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ловушка",
                                    color = NeonOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                currentDestination = PrankDestination.AI_PRANK
                                haptics.vibrateShort(25)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = NeonPurple.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "✨", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ИИ",
                                    color = NeonPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { showTipsDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Советы",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                PrankDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentDestination != destination) {
                                currentDestination = destination
                                haptics.vibrateShort(25)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.shortTitle,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = NeonYellow,
                            indicatorColor = NeonYellow,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentDestination,
                animationSpec = tween(220),
                label = "screen_transition"
            ) { dest ->
                when (dest) {
                    PrankDestination.SOUNDBOARD -> SoundboardScreen(
                        audioEngine = audioEngine,
                        haptics = haptics
                    )
                    PrankDestination.TASER -> TaserScreen(
                        audioEngine = audioEngine,
                        haptics = haptics,
                        flashlight = flashlight
                    )
                    PrankDestination.CLIPPER -> HairClipperScreen(
                        audioEngine = audioEngine,
                        haptics = haptics
                    )
                    PrankDestination.CRACKED_SCREEN -> CrackedScreenPrank(
                        audioEngine = audioEngine,
                        haptics = haptics
                    )
                    PrankDestination.LIE_DETECTOR -> LieDetectorScreen(
                        audioEngine = audioEngine,
                        haptics = haptics
                    )
                    PrankDestination.FAKE_CALL -> FakeCallScreen(
                        audioEngine = audioEngine,
                        haptics = haptics
                    )
                    PrankDestination.AI_PRANK -> AiPrankScreen(
                        audioEngine = audioEngine,
                        haptics = haptics
                    )
                    PrankDestination.PRANK_LINK -> PrankLinkTrapScreen(
                        audioEngine = audioEngine,
                        haptics = haptics
                    )
                }
            }
        }
    }

    // Auto-launch trap dialog if opened via shared prank link
    if (isTrapActiveOnLaunch) {
        val disguise = remember(trapDisguiseCode) {
            when (trapDisguiseCode) {
                "iq" -> DisguiseOption("Тест на уровень IQ", "Узнайте свой гениальный балл", "🧠", "Пройдите быстрый экспресс-тест на IQ. Нажмите «Начать»", "iq")
                "video" -> DisguiseOption("Секретное видео", "Закрытый приватный просмотр", "🔞", "Приватный видеофайл. Нажмите «Смотреть»", "video")
                "card" -> DisguiseOption("Праздничная открытка", "Вам прислали сюрприз", "🎉", "Кто-то отправил вам секретное послание! Открыть?", "card")
                else -> DisguiseOption("Подарок 5 000 ₽", "Вам начислен денежный приз!", "🎁", "Вам поступил перевод 5 000 рублей! Нажмите «Получить»", "gift")
            }
        }
        Dialog(
            onDismissRequest = { isTrapActiveOnLaunch = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PrankTrapExecutionView(
                durationMinutes = trapDurationMinutes.coerceAtLeast(10),
                disguise = disguise,
                soundCategory = "Случайный микс 🎲",
                audioEngine = audioEngine,
                haptics = haptics,
                onDismiss = { isTrapActiveOnLaunch = false }
            )
        }
    }

    // Prank Tips Dialog
    if (showTipsDialog) {
        Dialog(onDismissRequest = { showTipsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Как круто разыграть друзей",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val tips = listOf(
                        "✂️ Машинка для стрижки: включите мотор и проведите ребром телефона по затылку друга, удерживая палец на экране!",
                        "💣 Бомба с таймером: выберите пук или горн, установите 10с и положите телефон под подушку или в рюкзак.",
                        "🛋️ Подушка-пердушка: включите датчик движения и положите телефон на стул. Звук раздастся, когда кто-то сядет!",
                        "⚡ Шокер: включите фонарик и используйте в темноте — экран и вспышка создают вид реального электроразряда!",
                        "🧬 Детектор лжи: незаметно нажимайте верхний левый угол для Правды и правый для Лжи!",
                        "🔨 Разбитый экран: поставьте таймер на 5 секунд и попросите друга подержать телефон."
                    )

                    tips.forEach { tip ->
                        Text(
                            text = tip,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        onClick = { showTipsDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        color = NeonYellow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ПОНЯТНО, ПОГНАЛИ!",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
