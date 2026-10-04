package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PrankAudioEngine
import com.example.audio.PrankSoundType
import com.example.hardware.PrankHaptics
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

enum class CheatMode(val label: String) {
    RANDOM("Случайно"),
    FORCE_TRUE("Всегда Правда"),
    FORCE_LIE("Всегда Ложь")
}

@Composable
fun LieDetectorScreen(
    audioEngine: PrankAudioEngine,
    haptics: PrankHaptics
) {
    var cheatMode by remember { mutableStateOf(CheatMode.RANDOM) }
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var scanResult by remember { mutableStateOf<Boolean?>(null) } // true: Truth, false: Lie
    var simulatedHeartRate by remember { mutableIntStateOf(72) }

    // Laser bar scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_trans")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    // Scanning Coroutine Loop
    LaunchedEffect(isScanning) {
        if (isScanning) {
            scanResult = null
            scanProgress = 0f
            simulatedHeartRate = 75

            for (step in 1..30) {
                delay(100L)
                scanProgress = step / 30f
                simulatedHeartRate = 75 + (step * 2.2).toInt() + Random.nextInt(5)

                if (step % 5 == 0) {
                    haptics.vibrateHeartbeat()
                }
            }

            // Determine Result
            val finalResult = when (cheatMode) {
                CheatMode.FORCE_TRUE -> true
                CheatMode.FORCE_LIE -> false
                CheatMode.RANDOM -> Random.nextBoolean()
            }

            scanResult = finalResult
            isScanning = false

            if (finalResult) {
                audioEngine.playSound(PrankSoundType.DOORBELL) // Harmonic chime
                haptics.vibrateShort(200)
            } else {
                audioEngine.playSound(PrankSoundType.POLICE_SIREN) // Lie detected alarm!
                haptics.vibrateImpact()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Secret Cheat Bar (prankster control)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Secret Left Button (Tap to force truth)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        cheatMode = CheatMode.FORCE_TRUE
                        haptics.vibrateShort(30)
                    }
            )

            // Current Mode Indicator (Subtle so victim doesn't realize)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.clickable {
                    cheatMode = when (cheatMode) {
                        CheatMode.RANDOM -> CheatMode.FORCE_LIE
                        CheatMode.FORCE_LIE -> CheatMode.FORCE_TRUE
                        CheatMode.FORCE_TRUE -> CheatMode.RANDOM
                    }
                    haptics.vibrateShort(30)
                }
            ) {
                Text(
                    text = "⚙️ Режим: ${cheatMode.label}",
                    color = when (cheatMode) {
                        CheatMode.RANDOM -> TextMuted
                        CheatMode.FORCE_TRUE -> SuccessGreen
                        CheatMode.FORCE_LIE -> DangerRed
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Secret Right Button (Tap to force lie)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        cheatMode = CheatMode.FORCE_LIE
                        haptics.vibrateShort(30)
                    }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Heart Rate Monitor & ECG Line
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Пульс",
                            tint = if (isScanning) NeonPink else DangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ПУЛЬС / ЭКГ",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$simulatedHeartRate BPM",
                        color = if (simulatedHeartRate > 100) DangerRed else NeonGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ECG Graph Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    val path = Path()
                    val w = size.width
                    val h = size.height
                    val midY = h / 2

                    path.moveTo(0f, midY)
                    val points = 20
                    val dx = w / points

                    for (i in 1..points) {
                        val x = i * dx
                        val y = when {
                            i % 5 == 2 -> midY - (if (isScanning) 18f else 8f)
                            i % 5 == 3 -> midY + (if (isScanning) 16f else 6f)
                            else -> midY
                        }
                        path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = if (isScanning) NeonCyan else NeonGreen,
                        style = Stroke(width = 2.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fingerprint Scan Pad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(
                    2.dp,
                    when {
                        scanResult == true -> SuccessGreen
                        scanResult == false -> DangerRed
                        isScanning -> NeonCyan
                        else -> DarkSurfaceBorder
                    },
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive Fingerprint Area
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(2.dp, if (isScanning) NeonCyan else DarkSurfaceBorder, CircleShape)
                        .testTag("pad_fingerprint")
                        .pointerInput(isScanning) {
                            if (!isScanning) {
                                detectTapGestures(
                                    onPress = {
                                        isScanning = true
                                        tryAwaitRelease()
                                        // If released early before finish
                                        if (scanProgress < 0.95f) {
                                            isScanning = false
                                            scanProgress = 0f
                                        }
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Сканер отпечатка",
                        tint = when {
                            scanResult == true -> SuccessGreen
                            scanResult == false -> DangerRed
                            isScanning -> NeonCyan
                            else -> TextSecondary
                        },
                        modifier = Modifier.size(110.dp)
                    )

                    // Laser beam sweeping over fingerprint
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .offset { IntOffset(0, ((laserY - 0.5f) * 150.dp.toPx()).toInt()) }
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, NeonCyan, Color.White, NeonCyan, Color.Transparent)
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scan Instruction or Result
                if (isScanning) {
                    Text(
                        text = "СКАНИРОВАНИЕ... ${(scanProgress * 100).toInt()}%",
                        color = NeonCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Держите палец на сканере",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                } else if (scanResult != null) {
                    val isTruth = scanResult == true
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isTruth) SuccessGreen.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (isTruth) SuccessGreen else DangerRed
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isTruth) "100% ПРАВДА 😇" else "НАГЛАЯ ЛОЖЬ! 😈",
                                color = if (isTruth) SuccessGreen else DangerRed,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isTruth) "Детектор подтвердил честность" else "Зафиксировано учащение пульса и стресс",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "ПРИЛОЖИТЕ ПАЛЕЦ ДЛЯ ПРОВЕРКИ",
                        color = NeonYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Удерживайте палец до конца анализа",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reset Button
        if (scanResult != null) {
            Button(
                onClick = {
                    scanResult = null
                    scanProgress = 0f
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Повторить", tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "НОВЫЙ ТЕСТ", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
