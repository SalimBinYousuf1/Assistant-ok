package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroqModels
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.FrostCyanDark
import com.example.ui.theme.FrostCyanPrimary
import com.example.ui.theme.FrostDarkSurfaceSubtle
import com.example.ui.theme.FrostEmeraldSuccess
import com.example.ui.theme.FrostIrisAccent
import com.example.ui.theme.FrostLightSurfaceSubtle
import com.example.ui.theme.FrostRoseError
import com.example.viewmodel.AssistantViewModel
import com.example.viewmodel.KeyValidationState

@Composable
fun SettingsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentApiKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()
    val ttsPitch by viewModel.ttsPitch.collectAsState()
    val ttsSpeed by viewModel.ttsSpeed.collectAsState()
    val vibrateEnabled by viewModel.vibrateEnabled.collectAsState()
    val autoListenEnabled by viewModel.autoListenEnabled.collectAsState()
    val systemPrompt by viewModel.systemPrompt.collectAsState()
    val keyValidationState by viewModel.keyValidationState.collectAsState()
    val isDark = isSystemInDarkTheme()

    var inputKey by remember(currentApiKey) { mutableStateOf(currentApiKey) }
    var showPassword by remember { mutableStateOf(false) }
    var promptInput by remember(systemPrompt) { mutableStateOf(systemPrompt) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Assistant Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Configure Groq API key, reasoning models, voice behavior, and permissions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Groq API Key
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                isElevated = true
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = FrostCyanDark,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Groq API Key",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter your Groq Cloud API key (format: gsk_...). Keys are securely persisted in local device storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        placeholder = { Text("gsk_...") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassButton(
                            text = "Save Key",
                            onClick = {
                                viewModel.saveApiKey(inputKey)
                                Toast.makeText(context, "API Key saved", Toast.LENGTH_SHORT).show()
                            }
                        )

                        GlassButton(
                            text = "Test Connection",
                            icon = Icons.Default.NetworkCheck,
                            isPrimary = true,
                            onClick = {
                                viewModel.saveApiKey(inputKey)
                                viewModel.testApiKey(inputKey)
                            }
                        )
                    }

                    // Key Validation Status
                    when (val state = keyValidationState) {
                        is KeyValidationState.Testing -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying key with Groq Cloud...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is KeyValidationState.Valid -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FrostEmeraldSuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Valid & Connected! (${state.latencyMs}ms latency)", color = FrostEmeraldSuccess, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }
                        is KeyValidationState.Error -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = FrostRoseError, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Error: ${state.message}", color = FrostRoseError, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        KeyValidationState.Idle -> {}
                    }
                }
            }
        }

        // Section 2: Model Selection
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = FrostIrisAccent, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Groq Inference Model",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GroqModels.AVAILABLE_MODELS.forEach { modelInfo ->
                        val isSelected = selectedModel == modelInfo.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else if (isDark) FrostDarkSurfaceSubtle else FrostLightSurfaceSubtle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setModel(modelInfo.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setModel(modelInfo.id) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = modelInfo.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (modelInfo.isRecommended) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = FrostIrisAccent.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "RECOMMENDED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FrostIrisAccent,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = modelInfo.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${modelInfo.speedRating} • ${modelInfo.contextWindow}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FrostCyanDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Voice & Speech Settings
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = FrostCyanDark, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Speech & TTS Voice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Speak Responses Aloud", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Salim reads out responses with Android TTS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = ttsEnabled,
                            onCheckedChange = { viewModel.setTtsEnabled(it) }
                        )
                    }

                    if (ttsEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Speech Speed: ${String.format("%.1fx", ttsSpeed)}", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = ttsSpeed,
                            onValueChange = { viewModel.setTtsSpeed(it) },
                            valueRange = 0.5f..1.8f
                        )

                        Text("Speech Pitch: ${String.format("%.1fx", ttsPitch)}", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = ttsPitch,
                            onValueChange = { viewModel.setTtsPitch(it) },
                            valueRange = 0.6f..1.4f
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        GlassButton(
                            text = "Preview Voice",
                            icon = Icons.Default.VolumeUp,
                            onClick = { viewModel.previewTtsVoice() }
                        )
                    }
                }
            }
        }

        // Section 4: System Behaviors
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = FrostIrisAccent, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Assistant Summon Behavior",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Feedback", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Tactile vibration when assistant is triggered", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = vibrateEnabled,
                            onCheckedChange = { viewModel.setVibrateEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Listen on Launch", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Immediately start microphone on Assist trigger", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoListenEnabled,
                            onCheckedChange = { viewModel.setAutoListenEnabled(it) }
                        )
                    }
                }
            }
        }

        // Section 5: Custom System Prompt
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "System Prompt & Persona",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = {
                                viewModel.resetSystemPrompt()
                                promptInput = com.example.data.local.SettingsManager.DEFAULT_SYSTEM_PROMPT
                                Toast.makeText(context, "System prompt reset to default", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset prompt")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 8,
                        placeholder = { Text("Assistant instructions...") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    GlassButton(
                        text = "Save System Prompt",
                        onClick = {
                            viewModel.setSystemPrompt(promptInput)
                            Toast.makeText(context, "System prompt updated", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Section 6: System Permissions & Automation Integration
        item {
            val micGranted by viewModel.micPermissionGranted.collectAsState()
            val accActive by viewModel.accessibilityActive.collectAsState()
            val defAssist by viewModel.defaultAssistantSet.collectAsState()

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = FrostEmeraldSuccess, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Integration & Permissions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Salim gets permissions once and never repeatedly asks. You can manage or inspect system access below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Permission status items
                    PermissionStatusItem(
                        title = "Microphone Access",
                        statusText = if (micGranted) "Granted ✓" else "Not Granted",
                        isGranted = micGranted,
                        onAction = { viewModel.openAppSettings(context) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionStatusItem(
                        title = "Default Digital Assistant",
                        statusText = if (defAssist) "Active ✓" else "Not Set",
                        isGranted = defAssist,
                        onAction = { viewModel.openDefaultAssistantSettings(context) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionStatusItem(
                        title = "Accessibility Automation Service",
                        statusText = if (accActive) "Active ✓" else "Disabled",
                        isGranted = accActive,
                        onAction = { viewModel.openAccessibilitySettings(context) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    GlassButton(
                        text = "Open App System Settings",
                        onClick = { viewModel.openAppSettings(context) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusItem(
    title: String,
    statusText: String,
    isGranted: Boolean,
    onAction: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) FrostDarkSurfaceSubtle else FrostLightSurfaceSubtle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isGranted) FrostEmeraldSuccess.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.clickable(onClick = onAction)
            ) {
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGranted) FrostEmeraldSuccess else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
