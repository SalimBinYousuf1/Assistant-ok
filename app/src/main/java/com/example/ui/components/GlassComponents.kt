package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrostCyanDark
import com.example.ui.theme.FrostCyanPrimary
import com.example.ui.theme.FrostDarkBorder
import com.example.ui.theme.FrostDarkBorderHighlight
import com.example.ui.theme.FrostDarkSurface
import com.example.ui.theme.FrostDarkSurfaceElevated
import com.example.ui.theme.FrostDarkSurfaceSubtle
import com.example.ui.theme.FrostEmeraldSuccess
import com.example.ui.theme.FrostIrisAccent
import com.example.ui.theme.FrostLightBorder
import com.example.ui.theme.FrostLightBorderHighlight
import com.example.ui.theme.FrostLightSurface
import com.example.ui.theme.FrostLightSurfaceElevated
import com.example.ui.theme.FrostLightSurfaceSubtle
import com.example.ui.theme.FrostRoseError

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    isElevated: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        if (isElevated) FrostDarkSurfaceElevated else FrostDarkSurface
    } else {
        if (isElevated) FrostLightSurfaceElevated else FrostLightSurface
    }

    val borderColor = if (isDark) {
        if (isElevated) FrostDarkBorderHighlight else FrostDarkBorder
    } else {
        if (isElevated) FrostLightBorderHighlight else FrostLightBorder
    }

    val shape = RoundedCornerShape(cornerRadius)

    Surface(
        modifier = modifier
            .clip(shape)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            borderColor,
                            borderColor.copy(alpha = 0.2f)
                        )
                    )
                ),
                shape
            ),
        color = bgColor,
        shape = shape,
        tonalElevation = if (isElevated) 4.dp else 1.dp
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = false,
    testTag: String = "glass_button"
) {
    val isDark = isSystemInDarkTheme()
    val bgBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                FrostCyanPrimary,
                FrostIrisAccent
            )
        )
    } else {
        SolidColor(if (isDark) FrostDarkSurfaceSubtle else FrostLightSurfaceSubtle)
    }

    val borderStroke = BorderStroke(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = if (isPrimary) {
                listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.1f))
            } else {
                if (isDark) listOf(FrostDarkBorderHighlight, FrostDarkBorder)
                else listOf(FrostLightBorderHighlight, FrostLightBorder)
            }
        )
    )

    val shape = RoundedCornerShape(14.dp)
    val textColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .border(borderStroke, shape)
            .background(bgBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LiquidAssistantOrb(
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean,
    rmsDb: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Idle breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val idleBreathing by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_scale"
    )

    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_rotation"
    )

    // Dynamic scale driven by audio RMS decibels when listening
    val dynamicAudioScale by animateFloatAsState(
        targetValue = if (isListening) {
            1.0f + (rmsDb / 12f) * 0.35f
        } else if (isSpeaking) {
            1.1f
        } else {
            idleBreathing
        },
        animationSpec = tween(120),
        label = "audio_scale"
    )

    val coreGlowColor = when {
        isThinking -> FrostIrisAccent
        isListening -> FrostCyanDark
        isSpeaking -> FrostEmeraldSuccess
        else -> if (isDark) FrostCyanDark else FrostCyanPrimary
    }

    Box(
        modifier = modifier
            .size(130.dp)
            .testTag("liquid_assistant_orb")
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer refraction aura
        Box(
            modifier = Modifier
                .size(126.dp)
                .scale(dynamicAudioScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            coreGlowColor.copy(alpha = if (isListening) 0.38f else 0.16f),
                            coreGlowColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Middle frosted crystal ring
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(if (isListening) dynamicAudioScale * 0.95f else 1.0f)
                .clip(CircleShape)
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.sweepGradient(
                            listOf(
                                coreGlowColor.copy(alpha = 0.8f),
                                Color.White.copy(alpha = 0.9f),
                                coreGlowColor.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.8f),
                                coreGlowColor.copy(alpha = 0.8f)
                            )
                        )
                    ),
                    CircleShape
                )
                .background(
                    if (isDark) FrostDarkSurfaceElevated.copy(alpha = 0.7f)
                    else FrostLightSurfaceElevated.copy(alpha = 0.75f)
                )
        )

        // Center vibrant core
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            coreGlowColor,
                            FrostIrisAccent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val icon = when {
                isListening -> Icons.Default.Mic
                isSpeaking -> Icons.Default.Stop
                isThinking -> Icons.Default.HourglassBottom
                else -> Icons.Default.Mic
            }

            Icon(
                imageVector = icon,
                contentDescription = if (isListening) "Listening" else if (isSpeaking) "Stop Speaking" else "Tap to Speak",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun WaveformVisualizer(
    rmsDb: Float,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 7
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val baseRms = if (isListening) rmsDb.coerceIn(0.5f, 12f) else 1.0f
        val factors = listOf(0.4f, 0.7f, 1.0f, 1.3f, 1.0f, 0.7f, 0.4f)

        for (i in 0 until barCount) {
            val heightRatio = (baseRms / 12f) * factors[i]
            val animatedHeight by animateFloatAsState(
                targetValue = (heightRatio * 22f).coerceIn(4f, 26f),
                animationSpec = tween(90),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                FrostCyanDark,
                                FrostIrisAccent
                            )
                        )
                    )
            )
            if (i < barCount - 1) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

@Composable
fun ActionExecutionCard(
    actionType: String,
    summary: String,
    payload: String?,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val icon = when (actionType) {
        "ALARM" -> Icons.Default.Alarm
        "TIMER" -> Icons.Default.HourglassBottom
        "FLASHLIGHT" -> Icons.Default.FlashlightOn
        "APP_LAUNCH" -> Icons.Default.Apps
        "WEATHER" -> Icons.Default.WbSunny
        "SEARCH" -> Icons.Default.Search
        "PHONE_CALL" -> Icons.Default.Call
        "MESSAGE" -> Icons.Default.Message
        "DEVICE_STATUS" -> Icons.Default.BatteryChargingFull
        else -> Icons.Default.CheckCircle
    }

    val statusColor = if (isError) FrostRoseError else FrostEmeraldSuccess
    val title = when (actionType) {
        "ALARM" -> "Android Alarm Clock"
        "TIMER" -> "Countdown Timer"
        "FLASHLIGHT" -> "Device Flashlight"
        "APP_LAUNCH" -> "Application Launch"
        "WEATHER" -> "Live Weather Forecast"
        "SEARCH" -> "Web Search"
        "PHONE_CALL" -> "Phone Dialer"
        "MESSAGE" -> "Messaging Action"
        "DEVICE_STATUS" -> "System Diagnostics"
        else -> "Assistant Action"
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        isElevated = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    onSend: (() -> Unit)? = null,
    testTag: String = "glass_text_field"
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .border(
                BorderStroke(
                    1.dp,
                    if (isDark) FrostDarkBorderHighlight else FrostLightBorderHighlight
                ),
                shape
            )
            .background(if (isDark) FrostDarkSurfaceElevated else FrostLightSurfaceElevated)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            keyboardActions = KeyboardActions(onDone = { onSend?.invoke() }),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        )

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingIcon()
        }
    }
}

@Composable
fun MultiStepPlanCard(
    steps: List<com.example.data.model.AgentStep>,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) return

    val isDark = isSystemInDarkTheme()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        isElevated = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(FrostCyanPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = FrostCyanPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Autonomous Multi-Step Plan (${steps.size} steps)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Status indicator pill
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                when (step.status) {
                                    "EXECUTING" -> FrostIrisAccent.copy(alpha = 0.2f)
                                    "FAILED" -> FrostRoseError.copy(alpha = 0.18f)
                                    else -> FrostEmeraldSuccess.copy(alpha = 0.18f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (step.status) {
                            "EXECUTING" -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = FrostIrisAccent
                                )
                            }
                            "FAILED" -> {
                                Text(
                                    text = "✕",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostRoseError
                                )
                            }
                            else -> {
                                Text(
                                    text = "✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FrostEmeraldSuccess
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!step.observation.isNullOrBlank()) {
                            Text(
                                text = step.observation,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                if (index < steps.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun AppleStatusCapsule(
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = modifier
            .clip(shape)
            .border(
                1.dp,
                if (isDark) FrostDarkBorderHighlight else FrostLightBorderHighlight,
                shape
            ),
        color = if (isDark) FrostDarkSurfaceElevated.copy(alpha = 0.95f) else FrostLightSurfaceElevated.copy(alpha = 0.95f),
        shape = shape,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(13.dp),
                    strokeWidth = 1.5.dp,
                    color = FrostCyanDark
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(FrostEmeraldSuccess)
                )
                Spacer(modifier = Modifier.width(7.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
