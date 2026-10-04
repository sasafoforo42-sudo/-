package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.PrankAudioEngine
import com.example.hardware.FlashlightController
import com.example.hardware.PrankHaptics
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

@Composable
fun TaserScreen(
    audioEngine: PrankAudioEngine,
    haptics: PrankHaptics,
    flashlight: FlashlightController
) {
    var isSafetyOff by remember { mutableStateOf(true) }
    var isZapping by remember { mutableStateOf(false) }
    var voltageLevel by remember { mutableIntStateOf(1) } // 0: 50kV, 1: 100kV, 2: 500kV

    val voltages = listOf("50,000 V", "100,000 V", "500,000 V MAX")
    val sparkColors = listOf(
        listOf(NeonCyan, ElectricBlue, Color.White),
        listOf(NeonYellow, NeonCyan, Color.White),
        listOf(NeonPink, NeonYellow, Color.White)
    )

    // Stop audio, haptics and strobe if leaving screen or releasing
    DisposableEffect(Unit) {
        onDispose {
            audioEngine.stopLoop()
            haptics.stop()
            flashlight.stopStrobe()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sparks_anim")
    val sparkSeed by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spark_seed"
    )

    val currentSparks = sparkColors[voltageLevel]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Status Bar: Voltage selector and Safety switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ ${voltages[voltageLevel]}",
                        color = currentSparks.first(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Safety switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, if (isSafetyOff) NeonYellow.copy(alpha = 0.4f) else DarkSurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isSafetyOff) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = "Предохранитель",
                    tint = if (isSafetyOff) NeonYellow else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSafetyOff) "БОЕВОЙ РЕЖИМ" else "БЛОКИРОВКА",
                    color = if (isSafetyOff) NeonYellow else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isSafetyOff,
                    onCheckedChange = {
                        isSafetyOff = it
                        haptics.vibrateShort()
                        if (!it && isZapping) {
                            isZapping = false
                            audioEngine.stopLoop()
                            haptics.stop()
                            flashlight.stopStrobe()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonYellow,
                        checkedTrackColor = NeonYellow.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurface
                    ),
                    modifier = Modifier.scale(0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Voltage Level Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("50 kV", "100 kV", "500 kV MAX").forEachIndexed { index, title ->
                val isSelected = voltageLevel == index
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) currentSparks.first().copy(alpha = 0.2f) else DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) currentSparks.first() else DarkSurfaceBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .pointerInput(Unit) {
                            detectTapGestures {
                                voltageLevel = index
                                haptics.vibrateShort()
                                if (isZapping) {
                                    audioEngine.startTaserContinuous(index)
                                }
                            }
                        }
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) currentSparks.first() else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Taser Device Display & Electric Arc Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Screen Flash overlay when zapping
            if (isZapping) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    currentSparks[0].copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Lightning Spark Arc between electrodes
                Box(
                    modifier = Modifier
                        .size(width = 240.dp, height = 90.dp)
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isZapping) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val startX = size.width * 0.22f
                            val endX = size.width * 0.78f
                            val midY = size.height * 0.65f

                            // Draw 3-4 jagged lightning bolts
                            val boltCount = if (voltageLevel == 2) 5 else 3
                            for (b in 0 until boltCount) {
                                val path = Path()
                                path.moveTo(startX, midY)
                                val steps = 8
                                var curX = startX
                                var curY = midY
                                val dx = (endX - startX) / steps

                                for (s in 1 until steps) {
                                    curX += dx
                                    val jitterY = (Random.nextFloat() - 0.5f) * (36f + voltageLevel * 16f)
                                    curY = midY + jitterY
                                    path.lineTo(curX, curY)
                                }
                                path.lineTo(endX, midY)

                                val color = currentSparks[b % currentSparks.size]
                                drawPath(
                                    path = path,
                                    color = color,
                                    style = Stroke(width = if (b == 0) 5f else 3f)
                                )
                            }

                            // Glowing sparks particles
                            for (p in 0..12) {
                                val px = startX + Random.nextFloat() * (endX - startX)
                                val py = midY + (Random.nextFloat() - 0.5f) * 60f
                                drawCircle(
                                    color = Color.White,
                                    radius = Random.nextFloat() * 4f + 2f,
                                    center = Offset(px, py)
                                )
                            }
                        }
                    } else {
                        // Idle indicator
                        Text(
                            text = if (isSafetyOff) "⚡ ГОТОВ К РАЗРЯДУ ⚡" else "🔒 ПРЕДОХРАНИТЕЛЬ ВКЛЮЧЕН",
                            color = if (isSafetyOff) NeonCyan.copy(alpha = 0.7f) else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Taser Visual Representation (Image or Vector)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (isZapping) currentSparks.first() else DarkSurfaceBorder
                    ),
                    modifier = Modifier
                        .size(width = 240.dp, height = 240.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.img_taser),
                            contentDescription = "Шокер",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (isZapping) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(currentSparks.first().copy(alpha = 0.2f))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Giant Hold-to-Zap Button
        Box(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .size(width = 280.dp, height = 80.dp)
                .testTag("btn_taser_zap"),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(40.dp),
                color = when {
                    !isSafetyOff -> DarkSurfaceElevated
                    isZapping -> currentSparks.first()
                    else -> NeonYellow
                },
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (isZapping) Color.White else DarkSurfaceBorder
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isSafetyOff, voltageLevel) {
                        if (isSafetyOff) {
                            detectTapGestures(
                                onPress = {
                                    isZapping = true
                                    audioEngine.startTaserContinuous(voltageLevel)
                                    haptics.startContinuousShock()
                                    flashlight.startStrobe(50)

                                    tryAwaitRelease()

                                    isZapping = false
                                    audioEngine.stopLoop()
                                    haptics.stop()
                                    flashlight.stopStrobe()
                                }
                            )
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Разряд",
                        tint = if (!isSafetyOff) TextMuted else DarkBackground,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (!isSafetyOff) "СНИМИТЕ БЛОКИРОВКУ" else if (isZapping) "РАЗРЯД!!!" else "УДЕРЖИВАЙ ДЛЯ УДАРА",
                        color = if (!isSafetyOff) TextMuted else DarkBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
