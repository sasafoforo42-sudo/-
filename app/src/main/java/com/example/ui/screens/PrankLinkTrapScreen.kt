package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.hardware.PrankHaptics
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.random.Random

data class DisguiseOption(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val baitText: String,
    val code: String
)

@Composable
fun PrankLinkTrapScreen(
    audioEngine: PrankAudioEngine,
    haptics: PrankHaptics
) {
    val context = LocalContext.current

    // Settings for the prank link
    var selectedDurationMinutes by remember { mutableIntStateOf(10) } // Minimum 10 minutes as requested
    var selectedIntervalSec by remember { mutableIntStateOf(5) } // Every 5 seconds
    var selectedSoundCategory by remember { mutableStateOf("Случайный микс 🎲") }

    val disguiseOptions = remember {
        listOf(
            DisguiseOption("Подарок 5 000 ₽", "Вам начислен денежный приз!", "🎁", "Вам поступил перевод 5 000 рублей! Нажмите «Получить»", "gift"),
            DisguiseOption("Тест на уровень IQ", "Узнайте свой гениальный балл", "🧠", "Пройдите быстрый экспресс-тест на IQ. Нажмите «Начать»", "iq"),
            DisguiseOption("Секретное видео", "Закрытый приватный просмотр", "🔞", "Приватный видеофайл. Нажмите «Смотреть»", "video"),
            DisguiseOption("Праздничная открытка", "Вам прислали сюрприз", "🎉", "Кто-то отправил вам секретное послание! Открыть?", "card")
        )
    }

    var selectedDisguise by remember { mutableStateOf(disguiseOptions[0]) }
    var isTestTrapActive by remember { mutableStateOf(false) }

    val baseUrl = "https://ais-pre-mrpzof67aiqrndszqlhwqq-936045327250.europe-west2.run.app"
    val generatedPrankUrl = remember(selectedDurationMinutes, selectedSoundCategory, selectedDisguise) {
        "$baseUrl/?prank=trap&duration=$selectedDurationMinutes&disguise=${selectedDisguise.code}"
    }

    val shareText = remember(generatedPrankUrl, selectedDisguise) {
        "${selectedDisguise.baitText}\n👉 $generatedPrankUrl"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonOrange, NeonPink))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Ссылка",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ССЫЛКА-ЛОВУШКА",
                            color = NeonOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = DangerRed.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "МИН. 10 МИН",
                                color = DangerRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Друг переходит по ссылке — и начинаются бесконечные звуки!",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Duration Selection (Minimum 10 minutes)
            item {
                Text(
                    text = "⏱️ Сколько минут будут орать звуки (от 10 мин):",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 15, 30, 60).forEach { mins ->
                        val isSel = selectedDurationMinutes == mins
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) NeonOrange.copy(alpha = 0.2f) else DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) NeonOrange else DarkSurfaceBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDurationMinutes = mins }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$mins мин",
                                    color = if (isSel) NeonOrange else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (mins == 10) "Минимум" else "Хардкор",
                                    color = if (isSel) NeonOrange else TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Disguise / Bait selection
            item {
                Text(
                    text = "🎭 Маскировка ссылки (что увидит друг):",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    disguiseOptions.forEach { opt ->
                        val isSel = selectedDisguise == opt
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) NeonCyan else DarkSurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDisguise = opt }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = opt.emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = opt.title,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = opt.subtitle,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSel) {
                                    Text(text = "Выбрано", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Sound Type selection
            item {
                Text(
                    text = "🔊 Звуки при открытии ссылки:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                val soundPacks = listOf(
                    "Случайный микс 🎲",
                    "Симфония пуков 💨",
                    "Воздушный горн 📢",
                    "Полицейская сирена 🚨",
                    "Жуткий скример 😱",
                    "Шокер + Машинка ⚡"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(soundPacks) { pack ->
                        val isSel = selectedSoundCategory == pack
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedSoundCategory = pack },
                            label = { Text(pack, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonYellow,
                                selectedLabelColor = DarkBackground,
                                containerColor = DarkSurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // 4. Action Buttons (Copy Link, Share, Test)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonOrange.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🔗 Ссылка готова к отправке:",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = generatedPrankUrl,
                            color = NeonOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Copy Link Button
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Prank Trap Link", shareText)
                                clipboard.setPrimaryClip(clip)
                                haptics.vibrateShort()
                                Toast.makeText(context, "Ссылка скопирована! Отправьте её другу 😈", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_copy_prank_link")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Копировать", tint = DarkBackground)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "СКОПИРОВАТЬ ССЫЛКУ ДЛЯ ДРУГА",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Share Button
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Отправить ссылку другу")
                                    context.startActivity(shareIntent)
                                    haptics.vibrateShort()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_share_prank_link")
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Поделиться", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Поделиться", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Test Simulator Button
                            Button(
                                onClick = {
                                    isTestTrapActive = true
                                    haptics.vibrateShort()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_test_trap_mode")
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = "Тест", tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Тест ловушки", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // FULLSCREEN TRAP SIMULATOR
    if (isTestTrapActive) {
        Dialog(
            onDismissRequest = { /* Prevent normal dismiss */ },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PrankTrapExecutionView(
                durationMinutes = selectedDurationMinutes,
                disguise = selectedDisguise,
                soundCategory = selectedSoundCategory,
                audioEngine = audioEngine,
                haptics = haptics,
                onDismiss = { isTestTrapActive = false }
            )
        }
    }
}

@Composable
fun PrankTrapExecutionView(
    durationMinutes: Int,
    disguise: DisguiseOption,
    soundCategory: String,
    audioEngine: PrankAudioEngine,
    haptics: PrankHaptics,
    onDismiss: () -> Unit
) {
    var hasVictimTriggered by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(durationMinutes * 60) }
    var soundCountTriggered by remember { mutableIntStateOf(0) }
    var exitTapCount by remember { mutableIntStateOf(0) }

    // Strobe / Disco visual effects when triggered
    val infiniteTransition = rememberInfiniteTransition(label = "prank_strobe")
    val strobeColor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe"
    )

    // Stop all audio on dismiss
    DisposableEffect(Unit) {
        onDispose {
            audioEngine.stopLoop()
            haptics.stop()
        }
    }

    // Automatic trigger after 3 seconds even if victim doesn't tap
    LaunchedEffect(Unit) {
        delay(3200L)
        if (!hasVictimTriggered) {
            hasVictimTriggered = true
            audioEngine.playSound(PrankSoundType.AIR_HORN)
            haptics.vibrateImpact()
        }
    }

    // Countdown timer & Non-stop Sound Blasts loop
    LaunchedEffect(hasVictimTriggered) {
        if (hasVictimTriggered) {
            val soundList = when {
                soundCategory.contains("пук") -> listOf(
                    PrankSoundType.FART_CLASSIC,
                    PrankSoundType.FART_WET,
                    PrankSoundType.FART_EPIC,
                    PrankSoundType.FART_SNIPER
                )
                soundCategory.contains("горн") -> listOf(
                    PrankSoundType.AIR_HORN,
                    PrankSoundType.CAR_ALARM
                )
                soundCategory.contains("сирен") -> listOf(
                    PrankSoundType.POLICE_SIREN,
                    PrankSoundType.CAR_ALARM
                )
                soundCategory.contains("скример") -> listOf(
                    PrankSoundType.SCREAMER,
                    PrankSoundType.GLASS_BREAK
                )
                soundCategory.contains("Шокер") -> listOf(
                    PrankSoundType.TASER_ZAP,
                    PrankSoundType.HAIR_CLIPPER_BURST
                )
                else -> PrankSoundType.values().toList()
            }

            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--

                // Fire sounds every 3-5 seconds!
                if (remainingSeconds % 4 == 0) {
                    val s = soundList.random()
                    audioEngine.playSound(s)
                    haptics.vibrateImpact()
                    soundCountTriggered++
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (hasVictimTriggered) {
                    if (strobeColor > 0.5f) Color(0xFF1F082B) else Color(0xFF0B1428)
                } else {
                    DarkBackground
                }
            )
    ) {
        if (!hasVictimTriggered) {
            // BAIT CAMOUFLAGE SCREEN (What victim sees first)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = disguise.emoji, fontSize = 72.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = disguise.title,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = disguise.baitText,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        hasVictimTriggered = true
                        audioEngine.playSound(PrankSoundType.AIR_HORN)
                        haptics.vibrateImpact()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "ОТКРЫТЬ / ПОЛУЧИТЬ",
                        color = DarkBackground,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Загрузка 99%...",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        } else {
            // CHAOS TRAP SCREEN (Victim is trapped in sounds!)
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🤡 ТЫ ПОПАЛСЯ НА ПРАНК! 😈",
                        color = NeonYellow,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Звуковая ловушка активирована на $durationMinutes минут!",
                        color = NeonPink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Huge Countdown Timer
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(2.dp, DangerRed),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ДО КОНЦА ПРАНКА:",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timeString,
                                color = DangerRed,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Воспроизведено звуков: $soundCountTriggered",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "⚠️ Ошибка 404: кнопка «Выключить» не найдена!\nЗвуки играют сами по себе 😂",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                // Secret Exit Hint for creator
                Text(
                    text = "Секретный выход: тройной клик в правом верхнем углу",
                    color = Color(0xFF333340),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        }

        // Secret Exit Tap in Top Right Corner
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
                        onDismiss()
                        audioEngine.stopLoop()
                        haptics.stop()
                    }
                }
        )
    }
}
