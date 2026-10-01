package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.components.ActionExecutionCard
import com.example.ui.components.AppleStatusCapsule
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidAssistantOrb
import com.example.ui.components.MultiStepPlanCard
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.FrostCyanDark
import com.example.ui.theme.FrostCyanPrimary
import com.example.ui.theme.FrostDarkSurfaceSubtle
import com.example.ui.theme.FrostEmeraldSuccess
import com.example.ui.theme.FrostIrisAccent
import com.example.ui.theme.FrostLightSurfaceSubtle
import com.example.viewmodel.AssistantNavTab
import com.example.viewmodel.AssistantViewModel

@Composable
fun AssistantMainScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val rmsDb by viewModel.rmsDb.collectAsState()
    val partialText by viewModel.partialSpeechText.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val actionNotice by viewModel.actionNotice.collectAsState()
    val activePlanSteps by viewModel.activePlanSteps.collectAsState()
    val isDark = isSystemInDarkTheme()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom when messages or active plan steps update
    LaunchedEffect(messages.size, activePlanSteps.size) {
        val totalCount = messages.size + (if (activePlanSteps.isNotEmpty()) 1 else 0)
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount)
        }
    }

    val quickPrompts = listOf(
        "Set timer for 5 minutes" to Icons.Default.HourglassBottom,
        "Turn on flashlight" to Icons.Default.FlashlightOn,
        "What's the weather today?" to Icons.Default.WbSunny,
        "Check battery status" to Icons.Default.Bolt,
        "Set alarm for 7:00 AM" to Icons.Default.Alarm
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Salim",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Autonomous System Assistant • $selectedModel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Status Chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (apiKey.isNotBlank()) FrostEmeraldSuccess.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                modifier = Modifier.clickable {
                    viewModel.setTab(AssistantNavTab.SETTINGS)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (apiKey.isNotBlank()) FrostEmeraldSuccess
                                else MaterialTheme.colorScheme.error
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (apiKey.isNotBlank()) "Ready" else "Configure Key",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (apiKey.isNotBlank()) FrostEmeraldSuccess
                        else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Active Action or Error Notice Banner
        AnimatedVisibility(visible = actionNotice != null) {
            actionNotice?.let { notice ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 12.dp,
                    isElevated = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearActionNotice() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Conversational Feed & Hero Assistant Orb
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hero Orb Header
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 22.dp,
                    isElevated = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LiquidAssistantOrb(
                            isListening = isListening,
                            isThinking = isThinking,
                            isSpeaking = isSpeaking,
                            rmsDb = rmsDb,
                            onClick = {
                                if (isListening) {
                                    viewModel.stopListening()
                                } else if (isSpeaking) {
                                    viewModel.stopSpeaking()
                                } else {
                                    viewModel.startListening()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Waveform / Status text
                        if (isListening) {
                            WaveformVisualizer(rmsDb = rmsDb, isListening = true)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (partialText.isNotBlank()) "\"$partialText\"" else "Listening...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FrostCyanDark,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (isThinking) {
                            AppleStatusCapsule(
                                text = if (activePlanSteps.isNotEmpty()) "Autonomous planning & executing (${activePlanSteps.size} steps)..."
                                else "Salim is thinking with Groq LPU...",
                                isLoading = true
                            )
                        } else if (isSpeaking) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = FrostEmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Speaking response (Tap orb to stop)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FrostEmeraldSuccess
                                )
                            }
                        } else {
                            Text(
                                text = "Tap the orb to speak or type below",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Shortcut Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    items(quickPrompts) { (prompt, icon) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) FrostDarkSurfaceSubtle else FrostLightSurfaceSubtle,
                            modifier = Modifier.clickable {
                                viewModel.processPrompt(prompt, isVoice = false)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Chat & Action History items
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Salim Message", msg.content))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSpeak = {
                        viewModel.previewTtsVoice()
                    }
                )
            }

            // Live Active Multi-Step Execution Plan
            if (activePlanSteps.isNotEmpty() && isThinking) {
                item {
                    MultiStepPlanCard(
                        steps = activePlanSteps,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom Input Row
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            cornerRadius = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = "Ask Salim or command multi-step tasks...",
                    modifier = Modifier.weight(1f),
                    onSend = {
                        if (textInput.isNotBlank()) {
                            viewModel.processPrompt(textInput, isVoice = false)
                            textInput = ""
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.processPrompt(textInput, isVoice = false)
                            textInput = ""
                        } else {
                            if (isListening) viewModel.stopListening() else viewModel.startListening()
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    FrostCyanPrimary,
                                    FrostIrisAccent
                                )
                            )
                        )
                        .testTag("send_or_mic_button")
                ) {
                    Icon(
                        imageVector = if (textInput.isNotBlank()) Icons.AutoMirrored.Filled.Send
                        else if (isListening) Icons.Default.Stop
                        else Icons.Default.Mic,
                        contentDescription = "Send or Record",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val isDark = isSystemInDarkTheme()
    val steps = message.parseSteps()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                contentDescription = null,
                tint = if (isUser) FrostIrisAccent else FrostCyanDark,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isUser) "You" else "Salim",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (message.isVoice) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice input",
                    tint = FrostCyanDark,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // If the message has multi-step execution steps, display the plan card!
        if (steps.isNotEmpty()) {
            MultiStepPlanCard(
                steps = steps,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else if (!message.actionType.isNullOrBlank() && !message.actionSummary.isNullOrBlank()) {
            ActionExecutionCard(
                actionType = message.actionType,
                summary = message.actionSummary,
                payload = message.actionPayload,
                isError = message.isError,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        GlassCard(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .fillMaxWidth(if (isUser) 0.85f else 0.95f),
            cornerRadius = 14.dp,
            isElevated = !isUser
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                // Message action tools (Copy)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
