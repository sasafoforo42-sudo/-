package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class PrankDestination(
    val title: String,
    val shortTitle: String,
    val icon: ImageVector,
    val testTag: String
) {
    SOUNDBOARD("Звуковая доска", "Звуки", Icons.Default.VolumeUp, "nav_soundboard"),
    TASER("Электрошокер", "Шокер", Icons.Default.FlashOn, "nav_taser"),
    CLIPPER("Машинка для волос", "Стрижка", Icons.Default.ContentCut, "nav_clipper"),
    CRACKED_SCREEN("Разбитый экран", "Экран", Icons.Default.PhoneAndroid, "nav_cracked_screen"),
    LIE_DETECTOR("Детектор лжи", "Полиграф", Icons.Default.Fingerprint, "nav_lie_detector"),
    FAKE_CALL("Фейковый звонок", "Звонок", Icons.Default.Call, "nav_fake_call"),
    AI_PRANK("ИИ Пранкер", "ИИ Мозг", Icons.Default.AutoAwesome, "nav_ai_prank"),
    PRANK_LINK("Ссылка-ловушка", "Ловушка", Icons.Default.Link, "nav_prank_link")
}
