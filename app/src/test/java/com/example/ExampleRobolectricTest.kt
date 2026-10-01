package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SettingsManager
import com.example.data.model.GroqModels
import com.example.data.tools.SystemActionExecutor
import com.example.data.tools.ToolDefinitionProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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

        settings.setSelectedModel("llama-3.1-8b-instant")
        assertEquals("llama-3.1-8b-instant", settings.getSelectedModel())
    }

    @Test
    fun `verify tool definition provider contains assistant capabilities`() {
        val tools = ToolDefinitionProvider.getAssistantTools()
        val toolNames = tools.map { it.function.name }

        assertTrue(toolNames.contains("set_alarm"))
        assertTrue(toolNames.contains("set_timer"))
        assertTrue(toolNames.contains("toggle_flashlight"))
        assertTrue(toolNames.contains("open_app"))
        assertTrue(toolNames.contains("get_device_status"))
        assertTrue(toolNames.contains("get_weather"))
        assertTrue(toolNames.contains("search_web"))
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
}
