package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.PrankAudioEngine
import com.example.audio.PrankSoundType
import com.example.hardware.MotionDetector
import com.example.hardware.PrankHaptics
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SoundboardScreen(
    audioEngine: PrankAudioEngine,
    haptics: PrankHaptics
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Все") }
    var playingSound by remember { mutableStateOf<PrankSoundType?>(null) }
    var showBombDialog by remember { mutableStateOf(false) }
    var isStealthBombActive by remember { mutableStateOf(false) }
    var bombRemainingSeconds by remember { mutableIntStateOf(10) }
    var selectedBombSound by remember { mutableStateOf(PrankSoundType.FART_EPIC) }

    // Motion detector (Whoopee cushion mode)
    var isMotionArming by remember { mutableStateOf(false) }
    var motionTriggerCount by remember { mutableIntStateOf(0) }

    val motionDetector = remember {
        MotionDetector(context) {
            audioEngine.playSound(PrankSoundType.FART_WET)
            haptics.vibrateImpact()
            motionTriggerCount++
        }
    }

    DisposableEffect(isMotionArming) {
        if (isMotionArming) {
            motionDetector.startListening()
        } else {
            motionDetector.stopListening()
        }
        onDispose {
            motionDetector.stopListening()
        }
    }

    // Stealth Bomb Timer Countdown
    LaunchedEffect(isStealthBombActive, bombRemainingSeconds) {
        if (isStealthBombActive && bombRemainingSeconds > 0) {
            delay(1000L)
            bombRemainingSeconds--
            if (bombRemainingSeconds == 0) {
                audioEngine.playSound(selectedBombSound)
                haptics.vibrateImpact()
                isStealthBombActive = false
            }
        }
    }

    val categories = listOf("Все", "Пуки", "Сигналы", "Разрушения", "Быт", "Ужасы", "Люди", "Инструменты")
    val filteredSounds = remember(selectedCategory) {
        if (selectedCategory == "Все") {
            PrankSoundType.values().toList()
        } else {
            PrankSoundType.values().filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Quick Prank Action Buttons (Bomb & Whoopee Cushion)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                onClick = { showBombDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_timer_bomb"),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💣", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Бомба-таймер",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Скрытый взрыв",
                            color = NeonOrange,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Card(
                onClick = {
                    isMotionArming = !isMotionArming
                    haptics.vibrateShort()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_whoopee_motion"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMotionArming) NeonGreen.copy(alpha = 0.15f) else DarkSurfaceElevated
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMotionArming) NeonGreen else DarkSurfaceBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isMotionArming) NeonGreen.copy(alpha = 0.3f) else NeonPink.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isMotionArming) "🟢" else "🛋️", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isMotionArming) "На страже!" else "Подушка-пук",
                            color = if (isMotionArming) NeonGreen else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isMotionArming) "Датчик движения" else "При движении",
                            color = if (isMotionArming) NeonGreen else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (isMotionArming) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Датчик",
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Положите телефон на диван. При движении раздастся звук! (Сработало: $motionTriggerCount)",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isMotionArming = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Выключить",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Categories Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonYellow,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = DarkSurfaceBorder,
                        selectedBorderColor = NeonYellow
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sound Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(filteredSounds, key = { it.name }) { sound ->
                SoundTile(
                    sound = sound,
                    isPlaying = playingSound == sound,
                    onPlay = {
                        playingSound = sound
                        audioEngine.playSound(sound)
                        haptics.vibrateShort()
                    }
                )
            }
        }
    }

    // Bomb Setup Dialog
    if (showBombDialog) {
        Dialog(onDismissRequest = { showBombDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💣 Бомба с таймером",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonOrange
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Установите время, спрячьте телефон, и он взорвется громким звуком!",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Время задержки:",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 20, 45).forEach { sec ->
                            val isSel = bombRemainingSeconds == sec
                            OutlinedButton(
                                onClick = { bombRemainingSeconds = sec },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSel) NeonOrange.copy(alpha = 0.2f) else Color.Transparent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) NeonOrange else DarkSurfaceBorder
                                )
                            ) {
                                Text(
                                    text = "${sec}с",
                                    color = if (isSel) NeonOrange else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Звук взрыва:",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val bombSounds = listOf(
                        PrankSoundType.FART_EPIC,
                        PrankSoundType.AIR_HORN,
                        PrankSoundType.SCREAMER,
                        PrankSoundType.GLASS_BREAK,
                        PrankSoundType.POLICE_SIREN
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        bombSounds.forEach { s ->
                            val isSel = selectedBombSound == s
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) NeonOrange.copy(alpha = 0.25f) else DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) NeonOrange else Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBombSound = s }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = s.emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = s.displayName,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showBombDialog = false
                            isStealthBombActive = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_arm_bomb")
                    ) {
                        Text(
                            text = "ЗАЛОЖИТЬ БОМБУ (${bombRemainingSeconds} сек)",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Stealth Screen Overlay for Time Bomb
    if (isStealthBombActive) {
        Dialog(
            onDismissRequest = { /* Prevent normal dismiss */ },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var dismissTapCount by remember { mutableIntStateOf(0) }
            val timeString = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable {
                        dismissTapCount++
                        if (dismissTapCount >= 3) {
                            isStealthBombActive = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Camouflage clock
                    Text(
                        text = timeString,
                        color = Color(0xFF222225),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "🤫 Скрытный режим (${bombRemainingSeconds}s)",
                        color = Color(0xFF33333A),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Тройной клик для отмены",
                        color = Color(0xFF1F1F24),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SoundTile(
    sound: PrankSoundType,
    isPlaying: Boolean,
    onPlay: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(100),
        label = "tile_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .scale(scale)
            .testTag("sound_tile_${sound.name}")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onPlay
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPlaying) NeonCyan else DarkSurfaceBorder
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        DarkSurfaceBorder.copy(alpha = 0.8f),
                                        DarkSurfaceElevated
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = sound.emoji, fontSize = 24.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NeonYellow.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Воспроизвести",
                            tint = NeonYellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = sound.displayName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = sound.category,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
