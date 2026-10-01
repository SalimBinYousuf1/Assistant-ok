package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ActionExecutionCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidAssistantOrb
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.FrostCyanDark
import com.example.ui.theme.FrostIrisAccent
import com.example.viewmodel.AssistantNavTab
import com.example.viewmodel.AssistantViewModel

@Composable
fun AssistantQuickSheet(
    viewModel: AssistantViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val rmsDb by viewModel.rmsDb.collectAsState()
    val partialText by viewModel.partialSpeechText.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val isDark = isSystemInDarkTheme()

    val lastAssistantMessage = messages.lastOrNull { it.role == "assistant" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Intercept clicks so sheet doesn't dismiss
                ),
            cornerRadius = 28.dp,
            isElevated = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with dismiss & settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Salim Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (apiKey.isBlank()) {
                    Text(
                        text = "Groq API Key Required",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Please configure your Groq key to enable hands-free voice assistant features.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassButton(
                        text = "Open Settings",
                        icon = Icons.Default.Settings,
                        isPrimary = true,
                        onClick = {
                            viewModel.setTab(AssistantNavTab.SETTINGS)
                            onDismiss()
                        }
                    )
                } else {
                    LiquidAssistantOrb(
                        isListening = isListening,
                        isThinking = isThinking,
                        isSpeaking = isSpeaking,
                        rmsDb = rmsDb,
                        onClick = {
                            if (isListening) viewModel.stopListening()
                            else if (isSpeaking) viewModel.stopSpeaking()
                            else viewModel.startListening()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isListening) {
                        WaveformVisualizer(rmsDb = rmsDb, isListening = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (partialText.isNotBlank()) "\"$partialText\"" else "Listening...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = FrostCyanDark,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (isThinking) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = FrostIrisAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Running with Groq ultra-low latency...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FrostIrisAccent
                            )
                        }
                    } else if (lastAssistantMessage != null) {
                        // Display executed action card if present
                        if (!lastAssistantMessage.actionType.isNullOrBlank()) {
                            ActionExecutionCard(
                                actionType = lastAssistantMessage.actionType,
                                summary = lastAssistantMessage.actionSummary ?: "",
                                payload = lastAssistantMessage.actionPayload,
                                isError = lastAssistantMessage.isError,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Text(
                            text = lastAssistantMessage.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    } else {
                        Text(
                            text = "What can I do for you?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GlassButton(
                            text = if (isListening) "Cancel" else if (isSpeaking) "Stop Voice" else "Speak",
                            icon = if (isSpeaking) Icons.Default.Stop else Icons.Default.Mic,
                            isPrimary = !isListening && !isSpeaking,
                            onClick = {
                                if (isListening) viewModel.stopListening()
                                else if (isSpeaking) viewModel.stopSpeaking()
                                else viewModel.startListening()
                            }
                        )
                    }
                }
            }
        }
    }
}
