package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.FrostCyanDark
import com.example.ui.theme.FrostEmeraldSuccess
import com.example.ui.theme.FrostIrisAccent
import com.example.viewmodel.AssistantViewModel

@Composable
fun ToolsGuideScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val toolsList = listOf(
        GuideItem("Autonomous Multi-Step Tasks", Icons.Default.Assistant, "Understands complex natural-language chains, plans multi-step tasks, and executes autonomously", "\"Check weather in Chicago, and if it's raining set an alarm for 7 AM and open Maps\""),
        GuideItem("Alarm Clock", Icons.Default.Alarm, "Set alarms instantly by voice", "\"Set alarm for 6:45 AM labelled Gym\""),
        GuideItem("Countdown Timer", Icons.Default.HourglassBottom, "Manage cooking, workouts, and work sprints", "\"Set a timer for 15 minutes\""),
        GuideItem("System Gestures & Navigation", Icons.Default.TouchApp, "Autonomous navigation via accessibility (Home, Back, Recents, Notifications)", "\"Go home\", \"Go back\", \"Show notifications\""),
        GuideItem("Device Flashlight", Icons.Default.FlashlightOn, "Hands-free torch control via Camera API", "\"Turn on the flashlight\" or \"Turn off torch\""),
        GuideItem("App Launcher", Icons.Default.Apps, "Launch any installed app on your Android phone", "\"Open YouTube\", \"Launch Spotify\", \"Open Settings\""),
        GuideItem("Live Global Weather", Icons.Default.WbSunny, "Real-time temperature and weather forecasts", "\"What is the weather in Tokyo right now?\""),
        GuideItem("Device Status", Icons.Default.BatteryChargingFull, "Real-time battery level and charging state", "\"Check battery level\" or \"What's the system time?\""),
        GuideItem("Settings Panels", Icons.Default.OpenInNew, "Direct shortcuts to Android settings panels", "\"Open Wi-Fi settings\", \"Open Bluetooth\""),
        GuideItem("Calendar Scheduling", Icons.Default.Alarm, "Schedule calendar events and reminders", "\"Schedule doctor appointment in 120 minutes\""),
        GuideItem("Math & Calculation", Icons.Default.Search, "Compute mathematical expressions and arithmetic", "\"Calculate 1450 * 0.18\""),
        GuideItem("Web Search", Icons.Default.Search, "Instant Google / Web queries via browser", "\"Search web for latest aerospace news\""),
        GuideItem("Phone Calls", Icons.Default.Call, "Quickly dial contacts and phone numbers", "\"Call 555-0199\""),
        GuideItem("Text Messaging", Icons.Default.Message, "Draft SMS messages quickly", "\"Send message to 555-0123 saying I will arrive soon\"")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Assistant Setup & Capabilities",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configure Salim to replace Google Assistant across Android gestures and Quick Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Set as Default Assistant
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                isElevated = true
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = null,
                            tint = FrostCyanDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Set as Default Android Assistant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Open Android Settings > Apps > Default apps.\n2. Tap 'Digital assistant app'.\n3. Choose 'Salim' instead of Google Assistant.\n4. Now, swiping from corner or holding the power button immediately activates Salim!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    GlassButton(
                        text = "Open Android Assistant Settings",
                        icon = Icons.Default.OpenInNew,
                        isPrimary = true,
                        onClick = { viewModel.openDefaultAssistantSettings(context) }
                    )
                }
            }
        }

        // Section 2: Quick Settings Tile
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = FrostIrisAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Quick Settings Tile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Swipe down twice from top of screen > Tap Edit (pencil icon) > Add the 'Salim Assistant' tile. Now summon Salim from anywhere with one tap!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Section 3: Commands List
        item {
            Text(
                text = "Voice Commands & System Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(toolsList.size) { index ->
            val tool = toolsList[index]
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.title,
                        tint = FrostCyanDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tool.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Example: ${tool.example}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FrostEmeraldSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private data class GuideItem(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val example: String
)
