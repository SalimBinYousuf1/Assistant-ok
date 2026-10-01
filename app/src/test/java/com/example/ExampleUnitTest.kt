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
        assertEquals("llama-3.3-70b-versatile", GroqModels.DEFAULT_MODEL)
        assertTrue(GroqModels.AVAILABLE_MODELS.isNotEmpty())

        val recommended = GroqModels.AVAILABLE_MODELS.find { it.isRecommended }
        assertNotNull(recommended)
        assertEquals("llama-3.3-70b-versatile", recommended?.id)
    }

    @Test
    fun `verify ChatMessage creation and properties`() {
        val message = ChatMessage(
            role = "assistant",
            content = "Alarm set for 7:00 AM",
            modelUsed = "llama-3.3-70b-versatile",
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
