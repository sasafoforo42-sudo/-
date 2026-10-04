package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.PrankAudioEngine
import com.example.hardware.PrankHaptics
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt
import kotlin.random.Random

data class HairParticle(
    val x: Float,
    val y: Float,
    val length: Float,
    val angle: Float,
    val color: Color
)

@Composable
fun HairClipperScreen(
    audioEngine: PrankAudioEngine,
    haptics: PrankHaptics
) {
    var isPowerOn by remember { mutableStateOf(false) }
    var isTouchingHead by remember { mutableStateOf(false) }
    var hairStyle by remember { mutableStateOf("Под 0мм (Налысо)") }

    val hairParticles = remember { mutableStateListOf<HairParticle>() }

    // Sound & Haptic State Controller
    DisposableEffect(isPowerOn, isTouchingHead) {
        if (isPowerOn) {
            audioEngine.startClipperContinuous(isCutting = isTouchingHead)
            haptics.startContinuousClipper(isCutting = isTouchingHead)
        } else {
            audioEngine.stopLoop()
            haptics.stop()
        }
        onDispose {
            audioEngine.stopLoop()
            haptics.stop()
        }
    }

    // Blade vibration animation
    val infiniteTransition = rememberInfiniteTransition(label = "blade_vibe")
    val bladeOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(40, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blade_offset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top instruction / status
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isPowerOn) (if (isTouchingHead) NeonYellow else NeonGreen) else DarkSurfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isPowerOn) (if (isTouchingHead) NeonYellow else NeonGreen) else TextMuted)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!isPowerOn) "Машинка выключена" else if (isTouchingHead) "✂️ СТРИЖКА ВОЛОС ИДЁТ!" else "⚡ Мотор работает на холостых",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPowerOn) "Поднесите телефон к затылку и удерживайте экран" else "Нажмите кнопку питания для старта",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Interactive Clipper Area (Touch to Cut)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(
                    2.dp,
                    if (isTouchingHead) NeonYellow else DarkSurfaceBorder,
                    RoundedCornerShape(24.dp)
                )
                .testTag("area_hair_touch")
                .pointerInput(isPowerOn) {
                    if (isPowerOn) {
                        detectTapGestures(
                            onPress = { offset ->
                                isTouchingHead = true

                                // Spawn cut hairs
                                for (i in 0..16) {
                                    hairParticles.add(
                                        HairParticle(
                                            x = offset.x + (Random.nextFloat() - 0.5f) * 160f,
                                            y = offset.y + (Random.nextFloat() - 0.5f) * 120f,
                                            length = Random.nextFloat() * 25f + 10f,
                                            angle = Random.nextFloat() * 360f,
                                            color = if (Random.nextBoolean()) Color(0xFF2C1D11) else Color(0xFF141416)
                                        )
                                    )
                                }
                                if (hairParticles.size > 80) {
                                    hairParticles.removeRange(0, 30)
                                }

                                tryAwaitRelease()
                                isTouchingHead = false
                            }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Cut Hairs Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                hairParticles.forEach { p ->
                    val rad = Math.toRadians(p.angle.toDouble())
                    val endX = (p.x + p.length * Math.cos(rad)).toFloat()
                    val endY = (p.y + p.length * Math.sin(rad)).toFloat()
                    drawLine(
                        color = p.color,
                        start = Offset(p.x, p.y),
                        end = Offset(endX, endY),
                        strokeWidth = 3f
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Moving Blades at the top of the trimmer
                Box(
                    modifier = Modifier
                        .size(width = 180.dp, height = 30.dp)
                        .offset { IntOffset(if (isPowerOn) bladeOffset.roundToInt() * 2 else 0, 0) }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val teeth = 22
                        val toothW = size.width / teeth
                        for (i in 0 until teeth) {
                            if (i % 2 == 0) {
                                drawRect(
                                    color = Color(0xFFE2E8F0),
                                    topLeft = Offset(i * toothW, 0f),
                                    size = androidx.compose.ui.geometry.Size(toothW * 0.8f, size.height)
                                )
                            }
                        }
                    }
                }

                // Clipper Body Image
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.size(width = 220.dp, height = 260.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_clipper),
                        contentDescription = "Машинка",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isPowerOn) "ПРИЖМИ К ВОЛОСАМ ДРУГА!" else "ВКЛЮЧИ ПИТАНИЕ ВНИЗУ",
                    color = if (isPowerOn) NeonYellow else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Power Button ON/OFF
        Button(
            onClick = {
                isPowerOn = !isPowerOn
                haptics.vibrateShort()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("btn_clipper_power"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPowerOn) NeonGreen else DarkSurfaceElevated
            ),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (isPowerOn) NeonGreen else DarkSurfaceBorder
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Включение",
                    tint = if (isPowerOn) DarkBackground else NeonGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isPowerOn) "ВЫКЛЮЧИТЬ МАШИНКУ" else "ВКЛЮЧИТЬ МАШИНКУ",
                    color = if (isPowerOn) DarkBackground else TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
