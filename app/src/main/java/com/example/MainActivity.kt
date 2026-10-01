package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.AssistantMainScreen
import com.example.ui.screens.AssistantQuickSheet
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsGuideScreen
import com.example.ui.theme.FrostCyanDark
import com.example.ui.theme.FrostCyanPrimary
import com.example.ui.theme.FrostDarkBorder
import com.example.ui.theme.FrostDarkBorderHighlight
import com.example.ui.theme.FrostDarkSurfaceElevated
import com.example.ui.theme.FrostIrisAccent
import com.example.ui.theme.FrostLightBorder
import com.example.ui.theme.FrostLightBorderHighlight
import com.example.ui.theme.FrostLightSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AssistantNavTab
import com.example.viewmodel.AssistantViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AssistantViewModel by viewModels {
        AssistantViewModel.provideFactory(application as SalimApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleAssistantIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAssistantIntent(intent)
    }

    private fun handleAssistantIntent(intent: Intent?) {
        if (intent == null) return
        val isAssistAction = intent.action == Intent.ACTION_ASSIST ||
                intent.action == Intent.ACTION_VOICE_COMMAND ||
                intent.getBooleanExtra("EXTRA_SUMMON_IMMEDIATE", false)

        if (isAssistAction) {
            viewModel.setQuickSheetOpen(true)
        }
    }
}

@Composable
fun MainAppContent(viewModel: AssistantViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isQuickSheetOpen by viewModel.isQuickSheetOpen.collectAsState()
    val micPermissionGranted by viewModel.micPermissionGranted.collectAsState()
    val isDark = isSystemInDarkTheme()

    // Permission launcher for microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setMicPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    BackHandler(enabled = currentTab != AssistantNavTab.ASSISTANT || isQuickSheetOpen) {
        if (isQuickSheetOpen) {
            viewModel.setQuickSheetOpen(false)
        } else {
            viewModel.setTab(AssistantNavTab.ASSISTANT)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FloatingGlassNavBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) },
                onQuickSummon = { viewModel.setQuickSheetOpen(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AssistantNavTab.ASSISTANT -> AssistantMainScreen(viewModel = viewModel)
                AssistantNavTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                AssistantNavTab.TOOLS -> ToolsGuideScreen(viewModel = viewModel)
                AssistantNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }

            // Quick Sheet Assistant Overlay (for assist trigger, quick settings tile, or instant summon)
            AnimatedVisibility(
                visible = isQuickSheetOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AssistantQuickSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.setQuickSheetOpen(false) }
                )
            }
        }
    }
}

@Composable
fun FloatingGlassNavBar(
    currentTab: AssistantNavTab,
    onTabSelected: (AssistantNavTab) -> Unit,
    onQuickSummon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(26.dp)

    val bgColor = if (isDark) FrostDarkSurfaceElevated.copy(alpha = 0.88f)
    else FrostLightSurfaceElevated.copy(alpha = 0.92f)

    val borderColor = if (isDark) FrostDarkBorderHighlight else FrostLightBorderHighlight

    Surface(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape),
        color = bgColor,
        shape = shape,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton(
                icon = Icons.Default.SmartToy,
                label = "Assistant",
                isSelected = currentTab == AssistantNavTab.ASSISTANT,
                onClick = { onTabSelected(AssistantNavTab.ASSISTANT) },
                testTag = "nav_tab_assistant"
            )

            NavButton(
                icon = Icons.Default.History,
                label = "History",
                isSelected = currentTab == AssistantNavTab.HISTORY,
                onClick = { onTabSelected(AssistantNavTab.HISTORY) },
                testTag = "nav_tab_history"
            )

            // Center Quick Voice Summon Orb
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                FrostCyanPrimary,
                                FrostIrisAccent
                            )
                        )
                    )
                    .clickable(onClick = onQuickSummon)
                    .testTag("nav_quick_summon_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Instant Voice Assist",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            NavButton(
                icon = Icons.Default.Apps,
                label = "Tools",
                isSelected = currentTab == AssistantNavTab.TOOLS,
                onClick = { onTabSelected(AssistantNavTab.TOOLS) },
                testTag = "nav_tab_tools"
            )

            NavButton(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentTab == AssistantNavTab.SETTINGS,
                onClick = { onTabSelected(AssistantNavTab.SETTINGS) },
                testTag = "nav_tab_settings"
            )
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = if (isSystemInDarkTheme()) FrostCyanDark else FrostCyanPrimary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

    Column(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
