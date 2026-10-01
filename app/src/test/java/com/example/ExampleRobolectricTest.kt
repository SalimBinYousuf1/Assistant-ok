package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SettingsManager
import com.example.data.model.ChatMessage
import com.example.data.model.GroqModels
import com.example.data.tools.SystemActionExecutor
import com.example.data.tools.ToolDefinitionProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource is Salim`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim", appName)
    }

    @Test
    fun `verify settings manager defaults and updates`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settings = SettingsManager(context)

        assertEquals(GroqModels.DEFAULT_MODEL, settings.getSelectedModel())
        assertTrue(settings.isTtsEnabled())
        assertTrue(settings.isVibrateEnabled())

        settings.setApiKey("gsk_test_key_123456")
        assertEquals("gsk_test_key_123456", settings.getApiKey())

        settings.setSelectedModel("openai/gpt-oss-20b")
        assertEquals("openai/gpt-oss-20b", settings.getSelectedModel())

        assertFalse(settings.isOnboardingCompleted())
        settings.setOnboardingCompleted(true)
        assertTrue(settings.isOnboardingCompleted())
    }

    @Test
    fun `verify tool definition provider contains expanded assistant capabilities`() {
        val tools = ToolDefinitionProvider.getAssistantTools()
        val toolNames = tools.map { it.function.name }

        assertTrue(toolNames.contains("set_alarm"))
        assertTrue(toolNames.contains("set_timer"))
        assertTrue(toolNames.contains("toggle_flashlight"))
        assertTrue(toolNames.contains("open_app"))
        assertTrue(toolNames.contains("get_device_status"))
        assertTrue(toolNames.contains("get_weather"))
        assertTrue(toolNames.contains("search_web"))
        assertTrue(toolNames.contains("perform_system_gesture"))
        assertTrue(toolNames.contains("open_settings_page"))
        assertTrue(toolNames.contains("create_calendar_event"))
        assertTrue(toolNames.contains("calculate_math"))
        assertTrue(toolNames.contains("click_ui_element"))
    }

    @Test
    fun `verify device status tool execution`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val executor = SystemActionExecutor(context)
        val result = executor.execute("get_device_status", "{}")

        assertTrue(result.success)
        assertEquals("DEVICE_STATUS", result.toolType)
        assertNotNull(result.summary)
    }

    @Test
    fun `verify math calculation tool execution`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val executor = SystemActionExecutor(context)
        val result = executor.execute("calculate_math", """{"expression":"25 * 4 + 10"}""")

        assertTrue(result.success)
        assertEquals("MATH", result.toolType)
        assertEquals("110", result.payload)
    }

    @Test
    fun `verify ChatMessage multi-step plan parsing with Robolectric shadow`() {
        val stepsJson = """
            [
              {"stepIndex": 1, "title": "Checking weather", "toolName": "get_weather", "status": "SUCCESS", "observation": "18°C Sunny"},
              {"stepIndex": 2, "title": "Setting alarm", "toolName": "set_alarm", "status": "SUCCESS", "observation": "Alarm set for 7:00 AM"}
            ]
        """.trimIndent()

        val message = ChatMessage(
            role = "assistant",
            content = "Checked weather (18°C Sunny) and set your alarm for 7:00 AM.",
            modelUsed = "openai/gpt-oss-120b",
            isVoice = true,
            actionType = "ALARM",
            actionSummary = "Alarm set for 07:00",
            actionPayload = "7:00",
            stepsJson = stepsJson,
            isError = false
        )

        val steps = message.parseSteps()
        assertEquals(2, steps.size)
        assertEquals("Checking weather", steps[0].title)
        assertEquals("SUCCESS", steps[0].status)
        assertEquals("18°C Sunny", steps[0].observation)
        assertEquals("Setting alarm", steps[1].title)
    }
}
