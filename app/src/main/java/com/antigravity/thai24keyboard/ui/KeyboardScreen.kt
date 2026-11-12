package com.antigravity.thai24keyboard.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.thai24keyboard.model.KeyData
import com.antigravity.thai24keyboard.model.KeyboardLayouts
import com.antigravity.thai24keyboard.model.KeyboardMode
import com.antigravity.thai24keyboard.ui.theme.AccentBlue
import com.antigravity.thai24keyboard.ui.theme.DarkGrayBackground
import com.antigravity.thai24keyboard.ui.theme.DarkKeyActive
import com.antigravity.thai24keyboard.ui.theme.DarkKeyBackground
import com.antigravity.thai24keyboard.ui.theme.DarkTextPrimary
import com.antigravity.thai24keyboard.ui.theme.DarkTextSecondary
import com.antigravity.thai24keyboard.ui.theme.LightGrayBackground
import com.antigravity.thai24keyboard.ui.theme.LightKeyActive
import com.antigravity.thai24keyboard.ui.theme.LightKeyBackground
import com.antigravity.thai24keyboard.ui.theme.LightTextPrimary
import com.antigravity.thai24keyboard.ui.theme.LightTextSecondary
import com.antigravity.thai24keyboard.ui.theme.Thai24KeyboardTheme
import kotlinx.coroutines.delay

@Composable
fun KeyboardRootView(
    inputConnection: InputConnection?,
    imeOptions: Int,
    onSwitchIme: () -> Unit,
    onClose: () -> Unit
) {
    Thai24KeyboardTheme {
        KeyboardContent(
            inputConnection = inputConnection,
            imeOptions = imeOptions,
            onSwitchIme = onSwitchIme,
            onClose = onClose
        )
    }
}

@Composable
fun KeyboardContent(
    inputConnection: InputConnection?,
    imeOptions: Int,
    onSwitchIme: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(Vibrator::class.java)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    var currentMode by remember { mutableStateOf(KeyboardMode.THAI_24) }
    var isShiftActive by remember { mutableStateOf(false) }

    // Multi-tap state
    var lastKeyId by remember { mutableIntStateOf(-1) }
    var lastKeyTime by remember { mutableLongStateOf(0L) }
    var cycleIndex by remember { mutableIntStateOf(0) }
    val doubleTapTimeout = 380L // ms window for double-tap/multi-tap

    // Quick indicator popup showing typed char
    var popupChar by remember { mutableStateOf<String?>(null) }

    fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    // Auto-dismiss popup
    LaunchedEffect(popupChar) {
        if (popupChar != null) {
            delay(400)
            popupChar = null
        }
    }

    // Handle character input
    fun handleCharInput(key: KeyData, isLongPress: Boolean = false) {
        triggerHaptic()
        val ic = inputConnection ?: return
        val now = System.currentTimeMillis()

        if (currentMode == KeyboardMode.THAI_24) {
            if (isLongPress) {
                // Long press directly picks the secondary or tertiary
                val char = key.secondary ?: key.primary
                ic.commitText(char, 1)
                popupChar = char
                lastKeyId = -1
            } else {
                // Multi-tap check
                if (key.id == lastKeyId && (now - lastKeyTime) < doubleTapTimeout) {
                    // Advance to next char in cycle
                    cycleIndex = (cycleIndex + 1) % key.totalVariations
                    // Erase previous committed char
                    ic.deleteSurroundingText(1, 0)
                    val nextChar = key.getCharForCycle(cycleIndex)
                    ic.commitText(nextChar, 1)
                    popupChar = nextChar
                    lastKeyTime = now
                } else {
                    // First tap on this key
                    val firstChar = key.primary
                    ic.commitText(firstChar, 1)
                    popupChar = firstChar
                    lastKeyId = key.id
                    lastKeyTime = now
                    cycleIndex = 0
                }
            }
        } else {
            // English or Symbols
            val char = if (isShiftActive && key.secondary != null) key.secondary else key.primary
            ic.commitText(char, 1)
            popupChar = char
            lastKeyId = -1
        }
    }

    fun handleSpace() {
        triggerHaptic()
        lastKeyId = -1
        inputConnection?.commitText(" ", 1)
    }

    fun handleDelete() {
        triggerHaptic()
        lastKeyId = -1
        inputConnection?.deleteSurroundingText(1, 0)
    }

    fun handleEnter() {
        triggerHaptic()
        lastKeyId = -1
        val ic = inputConnection ?: return
        val action = imeOptions and EditorInfo.IME_MASK_ACTION
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            ic.commitText("\n", 1)
        }
    }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) DarkGrayBackground else LightGrayBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top status / popup preview bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (popupChar != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentBlue,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = popupChar ?: "",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = when (currentMode) {
                        KeyboardMode.THAI_24 -> "แป้นพิมพ์ไทย 24 ปุ่ม (แตะซ้ำพิมพ์ตัวบน)"
                        KeyboardMode.ENGLISH -> "English Keyboard"
                        KeyboardMode.SYMBOLS -> "ตัวเลขและสัญลักษณ์"
                    },
                    fontSize = 11.sp,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Keyboard Rows
        when (currentMode) {
            KeyboardMode.THAI_24 -> {
                KeyboardLayouts.thai24Rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { key ->
                            ThaiKeyButton(
                                key = key,
                                modifier = Modifier.weight(1f),
                                onClick = { handleCharInput(key) },
                                onLongClick = { handleCharInput(key, isLongPress = true) }
                            )
                        }
                    }
                }
            }
            KeyboardMode.ENGLISH -> {
                KeyboardLayouts.englishRows.forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (rowIndex == 2) {
                            // Shift key
                            FunctionalKeyButton(
                                text = if (isShiftActive) "⬆" else "⇧",
                                modifier = Modifier.weight(1.4f),
                                isActive = isShiftActive,
                                onClick = {
                                    triggerHaptic()
                                    isShiftActive = !isShiftActive
                                }
                            )
                        }

                        row.forEach { key ->
                            KeyButton(
                                primary = if (isShiftActive) key.secondary ?: key.primary else key.primary,
                                secondary = null,
                                modifier = Modifier.weight(1f),
                                onClick = { handleCharInput(key) },
                                onLongClick = { handleCharInput(key, isLongPress = true) }
                            )
                        }

                        if (rowIndex == 2) {
                            // Backspace key
                            FunctionalKeyButton(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                modifier = Modifier.weight(1.4f),
                                onClick = { handleDelete() }
                            )
                        }
                    }
                }
            }
            KeyboardMode.SYMBOLS -> {
                KeyboardLayouts.symbolRows.forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { key ->
                            KeyButton(
                                primary = key.primary,
                                secondary = null,
                                modifier = Modifier.weight(1f),
                                onClick = { handleCharInput(key) },
                                onLongClick = { handleCharInput(key, isLongPress = true) }
                            )
                        }

                        if (rowIndex == 2) {
                            FunctionalKeyButton(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                modifier = Modifier.weight(1.3f),
                                onClick = { handleDelete() }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode toggle button (TH / EN / 123)
            FunctionalKeyButton(
                text = when (currentMode) {
                    KeyboardMode.THAI_24 -> "EN"
                    KeyboardMode.ENGLISH -> "ไทย"
                    KeyboardMode.SYMBOLS -> "ไทย"
                },
                modifier = Modifier.weight(1.2f),
                onClick = {
                    triggerHaptic()
                    lastKeyId = -1
                    currentMode = if (currentMode == KeyboardMode.THAI_24) KeyboardMode.ENGLISH else KeyboardMode.THAI_24
                }
            )

            // Symbols toggle button (?123)
            FunctionalKeyButton(
                text = if (currentMode == KeyboardMode.SYMBOLS) "ABC" else "?123",
                modifier = Modifier.weight(1.1f),
                onClick = {
                    triggerHaptic()
                    lastKeyId = -1
                    currentMode = if (currentMode == KeyboardMode.SYMBOLS) KeyboardMode.THAI_24 else KeyboardMode.SYMBOLS
                }
            )

            // Switch IME / Language picker
            FunctionalKeyButton(
                icon = Icons.Default.Language,
                modifier = Modifier.weight(0.9f),
                onClick = {
                    triggerHaptic()
                    onSwitchIme()
                }
            )

            // Spacebar
            KeyButton(
                primary = "วรรค",
                secondary = null,
                icon = Icons.Default.SpaceBar,
                modifier = Modifier.weight(3.6f),
                onClick = { handleSpace() },
                onLongClick = { handleSpace() }
            )

            // Backspace key (always available in Thai mode bottom row)
            if (currentMode == KeyboardMode.THAI_24) {
                FunctionalKeyButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    modifier = Modifier.weight(1.2f),
                    onClick = { handleDelete() }
                )
            }

            // Return / Enter key
            FunctionalKeyButton(
                icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                isPrimaryAction = true,
                modifier = Modifier.weight(1.3f),
                onClick = { handleEnter() }
            )

            // Hide keyboard button
            FunctionalKeyButton(
                icon = Icons.Default.KeyboardHide,
                modifier = Modifier.weight(0.9f),
                onClick = {
                    triggerHaptic()
                    onClose()
                }
            )
        }
    }
}

/**
 * Specially designed Key button for Thai 24-key layout:
 * - Shows primary character large in center
 * - Shows secondary character (double-tap) in top-right corner with accent color
 * - Shows tertiary character in top-left corner if available
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThaiKeyButton(
    key: KeyData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val keyBg = if (isDark) DarkKeyBackground else LightKeyBackground
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) AccentBlue else Color(0xFF0056B3)
    val textTertiary = if (isDark) DarkTextSecondary else LightTextSecondary

    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(1.5.dp, RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(keyBg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        // Tertiary (top left)
        if (key.tertiary != null) {
            Text(
                text = key.tertiary,
                fontSize = 10.sp,
                color = textTertiary,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // Secondary (top right, double-tap shortcut indicator)
        if (key.secondary != null) {
            Text(
                text = key.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        // Primary (center)
        Text(
            text = key.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyButton(
    primary: String,
    secondary: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val keyBg = if (isDark) DarkKeyBackground else LightKeyBackground
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary

    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(1.5.dp, RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(keyBg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (secondary != null) {
            Text(
                text = secondary,
                fontSize = 10.sp,
                color = textSecondary,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        if (icon != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = primary,
                    tint = textPrimary,
                    modifier = Modifier.size(18.dp)
                )
                if (primary.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = primary,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                }
            }
        } else {
            Text(
                text = primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
        }
    }
}

@Composable
fun FunctionalKeyButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    isActive: Boolean = false,
    isPrimaryAction: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bg = when {
        isPrimaryAction -> AccentBlue
        isActive -> if (isDark) DarkKeyActive else LightKeyActive
        else -> if (isDark) Color(0xFF232528) else Color(0xFFC7CBD1)
    }

    val contentColor = when {
        isPrimaryAction -> Color.White
        isActive -> AccentBlue
        else -> if (isDark) DarkTextPrimary else LightTextPrimary
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(1.dp, RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text ?: "",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
