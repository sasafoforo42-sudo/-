package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.audio.PrankAudioEngine
import com.example.audio.PrankSoundType
import com.example.hardware.MotionDetector
import com.example.hardware.PrankHaptics
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

enum class CrackTriggerMode(val title: String, val desc: String) {
    ON_TOUCH("По касанию экрана", "Разбивается при первом же касании"),
    ON_SHAKE("По встряхиванию", "Разбивается, когда телефон качнут"),
    ON_TIMER("Таймер 5 сек", "Разбивается через 5 секунд в руках друга")
}

@Composable
fun CrackedScreenPrank(
    audioEngine: PrankAudioEngine,
    haptics: PrankHaptics
) {
    val context = LocalContext.current
    var selectedTrigger by remember { mutableStateOf(CrackTriggerMode.ON_TOUCH) }
    var isPrankArmed by remember { mutableStateOf(false) }
    var isCracked by remember { mutableStateOf(false) }
    var exitTapCount by remember { mutableIntStateOf(0) }

    val motionDetector = remember {
        MotionDetector(context) {
            if (isPrankArmed && selectedTrigger == CrackTriggerMode.ON_SHAKE && !isCracked) {
                isCracked = true
                audioEngine.playSound(PrankSoundType.GLASS_BREAK)
                haptics.vibrateImpact()
            }
        }
    }

    DisposableEffect(isPrankArmed, selectedTrigger) {
        if (isPrankArmed && selectedTrigger == CrackTriggerMode.ON_SHAKE) {
            motionDetector.startListening()
        } else {
            motionDetector.stopListening()
        }
        onDispose {
            motionDetector.stopListening()
        }
    }

    // Timer Trigger Countdown
    LaunchedEffect(isPrankArmed, selectedTrigger) {
        if (isPrankArmed && selectedTrigger == CrackTriggerMode.ON_TIMER && !isCracked) {
            delay(5000L)
            isCracked = true
            audioEngine.playSound(PrankSoundType.GLASS_BREAK)
            haptics.vibrateImpact()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔨 Пранк «Разбитый экран»",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = "Реалистичный звук трескающегося стекла и паутина трещин на весь дисплей!",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Crack Preview Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_broken_screen),
                    contentDescription = "Превью трещин",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay badge
                Surface(
                    color = DarkBackground.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💥 Реалистичная трещина",
                            color = NeonPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Выход из пранка: 3 быстрых тапа в правом верхнем углу",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger Mode Selector
        Text(
            text = "Способ срабатывания:",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CrackTriggerMode.values().forEach { mode ->
                val isSelected = selectedTrigger == mode
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NeonPink.copy(alpha = 0.15f) else DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) NeonPink else DarkSurfaceBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTrigger = mode }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                CrackTriggerMode.ON_TOUCH -> Icons.Default.TouchApp
                                CrackTriggerMode.ON_SHAKE -> Icons.Default.Sensors
                                CrackTriggerMode.ON_TIMER -> Icons.Default.Timer
                            },
                            contentDescription = mode.title,
                            tint = if (isSelected) NeonPink else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = mode.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = mode.desc,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Start / Arm Prank Button
        Button(
            onClick = {
                isCracked = false
                exitTapCount = 0
                isPrankArmed = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_arm_cracked_screen")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Запустить",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "АКТИВИРОВАТЬ ПРАНК",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }

    // Fullscreen Prank Overlay
    if (isPrankArmed) {
        Dialog(
            onDismissRequest = { /* No auto dismiss */ },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isCracked && selectedTrigger == CrackTriggerMode.ON_TOUCH) {
                            isCracked = true
                            audioEngine.playSound(PrankSoundType.GLASS_BREAK)
                            haptics.vibrateImpact()
                        }
                    }
            ) {
                // If not cracked yet, show innocent camouflage screen
                if (!isCracked) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Экран",
                            tint = NeonYellow,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Проверка сенсора экрана...",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (selectedTrigger) {
                                CrackTriggerMode.ON_TOUCH -> "Коснитесь экрана в любой точке для проверки"
                                CrackTriggerMode.ON_SHAKE -> "Слегка встряхните телефон"
                                CrackTriggerMode.ON_TIMER -> "Идёт диагностика системы (5 сек)..."
                            },
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Shattered Screen!
                    Image(
                        painter = painterResource(id = R.drawable.img_broken_screen),
                        contentDescription = "Разбитый экран",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Secret Exit Trigger in Top Right Corner (triple click)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(80.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            exitTapCount++
                            if (exitTapCount >= 3) {
                                isPrankArmed = false
                                isCracked = false
                                haptics.vibrateShort()
                            }
                        }
                )
            }
        }
    }
}
