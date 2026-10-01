package com.example

import com.example.data.model.ChatMessage
import com.example.data.model.GroqModels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify default Groq model configuration`() {
        assertEquals("openai/gpt-oss-120b", GroqModels.DEFAULT_MODEL)
        assertEquals(4, GroqModels.AVAILABLE_MODELS.size)

        val modelIds = GroqModels.AVAILABLE_MODELS.map { it.id }
        assertTrue(modelIds.contains("openai/gpt-oss-120b"))
        assertTrue(modelIds.contains("openai/gpt-oss-20b"))
        assertTrue(modelIds.contains("groq/compound"))
        assertTrue(modelIds.contains("qwen/qwen3.8-27b"))

        val recommended = GroqModels.AVAILABLE_MODELS.find { it.isRecommended }
        assertNotNull(recommended)
        assertEquals("openai/gpt-oss-120b", recommended?.id)
    }

    @Test
    fun `verify ChatMessage creation and properties`() {
        val message = ChatMessage(
            role = "assistant",
            content = "Alarm set for 7:00 AM",
            modelUsed = "openai/gpt-oss-120b",
            isVoice = true,
            actionType = "ALARM",
            actionSummary = "Alarm set for 07:00",
            actionPayload = "7:00",
            isError = false
        )

        assertEquals("assistant", message.role)
        assertTrue(message.isVoice)
        assertFalse(message.isError)
        assertEquals("ALARM", message.actionType)
    }
}
