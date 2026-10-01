package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "assistant", "system", "tool"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "",
    val isVoice: Boolean = false,
    val actionType: String? = null, // e.g. "ALARM", "TIMER", "WEATHER", "APP_LAUNCH", "FLASHLIGHT", "PHONE_CALL", "SEARCH"
    val actionSummary: String? = null,
    val actionPayload: String? = null,
    val isError: Boolean = false
)
