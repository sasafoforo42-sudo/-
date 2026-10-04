package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.hardware.PrankHaptics
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

data class CallerPreset(
    val name: String,
    val number: String,
    val subtitle: String,
    val avatarRes: Int? = null,
    val emoji: String = "👤"
)

@Composable
fun FakeCallScreen(
    audioEngine: PrankAudioEngine,
    haptics: PrankHaptics
) {
    val presets = remember {
        listOf(
            CallerPreset("Секретный Агент", "+7 (900) 007-00-00", "Спецслужба", R.drawable.img_caller_avatar),
            CallerPreset("Мама ❤️", "+7 (912) 345-67-89", "Мобильный", emoji = "👩"),
            CallerPreset("Босс / Директор", "+7 (495) 888-99-00", "Рабочий", emoji = "💼"),
            CallerPreset("Полиция 112", "112", "Экстренная служба", emoji = "👮"),
            CallerPreset("Илон Маск", "+1 (650) 555-0199", "Tesla Motors", emoji = "🚀"),
            CallerPreset("Курьер Пиццы", "+7 (999) 777-11-22", "Доставка", emoji = "🍕")
        )
    }

    var selectedCaller by remember { mutableStateOf(presets[0]) }
    var delaySeconds by remember { mutableIntStateOf(5) }
    var isTimerWaiting by remember { mutableStateOf(false) }
    var remainingWait by remember { mutableIntStateOf(5) }

    // Call Screen States
    var isCallRinging by remember { mutableStateOf(false) }
    var isCallActive by remember { mutableStateOf(false) }
    var callDurationSeconds by remember { mutableIntStateOf(0) }

    // Clean up sounds
    DisposableEffect(Unit) {
        onDispose {
            haptics.stop()
        }
    }

    // Timer Countdown to incoming call
    LaunchedEffect(isTimerWaiting, remainingWait) {
        if (isTimerWaiting && remainingWait > 0) {
            delay(1000L)
            remainingWait--
            if (remainingWait == 0) {
                isTimerWaiting = false
                isCallRinging = true
            }
        }
    }

    // Ringing Loop
    LaunchedEffect(isCallRinging) {
        if (isCallRinging) {
            while (isCallRinging) {
                audioEngine.playSound(PrankSoundType.PHONE_RING)
                haptics.vibrateHeartbeat()
                delay(2600L)
            }
        }
    }

    // Active Call Duration Counter
    LaunchedEffect(isCallActive) {
        if (isCallActive) {
            callDurationSeconds = 0
            while (isCallActive) {
                delay(1000L)
                callDurationSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(
            text = "📞 Фейковый входящий звонок",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = "Настройте звонящего, положите телефон на стол и разыграйте друзей!",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Caller Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp),
                    color = DarkSurface
                ) {
                    if (selectedCaller.avatarRes != null) {
                        Image(
                            painter = painterResource(id = selectedCaller.avatarRes!!),
                            contentDescription = "Аватар",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(text = selectedCaller.emoji, fontSize = 28.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedCaller.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedCaller.number,
                        color = NeonCyan,
                        fontSize = 13.sp
                    )
                    Text(
                        text = selectedCaller.subtitle,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Delay Picker
        Text(
            text = "Задержка перед звонком:",
            color = TextMuted,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                0 to "Сейчас",
                5 to "5 сек",
                15 to "15 сек",
                30 to "30 сек"
            ).forEach { (sec, label) ->
                val isSel = delaySeconds == sec
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) NeonCyan else DarkSurfaceBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { delaySeconds = sec }
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) NeonCyan else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Callers List
        Text(
            text = "Выберите звонящего:",
            color = TextMuted,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                val isSel = selectedCaller == preset
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) NeonCyan else DarkSurfaceBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCaller = preset }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (preset.avatarRes != null) "🕵️" else preset.emoji,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = preset.subtitle,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trigger Call Button
        Button(
            onClick = {
                if (delaySeconds == 0) {
                    isCallRinging = true
                } else {
                    remainingWait = delaySeconds
                    isTimerWaiting = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_trigger_fake_call")
        ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = "Позвонить", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isTimerWaiting) "ОЖИДАНИЕ ($remainingWait с)..." else "ЗАПУСТИТЬ ЗВОНОК",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }

    // Fullscreen Incoming Call Dialog
    if (isCallRinging) {
        Dialog(
            onDismissRequest = { /* No auto dismiss */ },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val pulseAnim = rememberInfiniteTransition(label = "ring_pulse")
            val pulseScale by pulseAnim.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF07090E))
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Caller Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(110.dp)
                                .border(2.dp, NeonCyan, CircleShape),
                            color = DarkSurface
                        ) {
                            if (selectedCaller.avatarRes != null) {
                                Image(
                                    painter = painterResource(id = selectedCaller.avatarRes!!),
                                    contentDescription = "Аватар",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(text = selectedCaller.emoji, fontSize = 48.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = selectedCaller.name,
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedCaller.number,
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Входящий вызов...",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Bottom Accept / Decline Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decline Button (Red)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(DangerRed)
                                    .clickable {
                                        isCallRinging = false
                                        haptics.vibrateShort()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Отклонить",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Отклонить", color = TextSecondary, fontSize = 12.sp)
                        }

                        // Accept Button (Green with pulsing animation)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                                    .clickable {
                                        isCallRinging = false
                                        isCallActive = true
                                        haptics.vibrateShort()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Принять",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Ответить", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Active Call Dialog
    if (isCallActive) {
        Dialog(
            onDismissRequest = { /* No auto dismiss */ },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val minutes = callDurationSeconds / 60
            val seconds = callDurationSeconds % 60
            val durationFormatted = String.format("%02d:%02d", minutes, seconds)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF07090E))
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Caller Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(90.dp)
                                .border(2.dp, SuccessGreen, CircleShape),
                            color = DarkSurface
                        ) {
                            if (selectedCaller.avatarRes != null) {
                                Image(
                                    painter = painterResource(id = selectedCaller.avatarRes!!),
                                    contentDescription = "Аватар",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(text = selectedCaller.emoji, fontSize = 40.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = selectedCaller.name,
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = durationFormatted,
                            color = SuccessGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Middle Keypad Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            Icons.Default.Mic to "Микрофон",
                            Icons.Default.Dialpad to "Клавиши",
                            Icons.Default.VolumeUp to "Динамик"
                        ).forEach { (icon, title) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = icon, contentDescription = title, tint = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = title, color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }

                    // End Call Button
                    Box(
                        modifier = Modifier
                            .padding(bottom = 30.dp)
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(DangerRed)
                            .clickable {
                                isCallActive = false
                                haptics.vibrateShort()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Завершить",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}
