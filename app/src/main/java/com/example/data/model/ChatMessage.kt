package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class AgentStep(
    val stepIndex: Int,
    val title: String,
    val toolName: String,
    val status: String, // "PENDING", "EXECUTING", "SUCCESS", "FAILED"
    val observation: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user", "assistant", "system", "tool"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "",
    val isVoice: Boolean = false,
    val actionType: String? = null,
    val actionSummary: String? = null,
    val actionPayload: String? = null,
    val stepsJson: String? = null,
    val isError: Boolean = false
) {
    fun parseSteps(): List<AgentStep> {
        if (stepsJson.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(stepsJson)
            val list = mutableListOf<AgentStep>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AgentStep(
                        stepIndex = obj.optInt("stepIndex", i + 1),
                        title = obj.optString("title", "Step ${i + 1}"),
                        toolName = obj.optString("toolName", ""),
                        status = obj.optString("status", "SUCCESS"),
                        observation = if (obj.has("observation")) obj.getString("observation") else null
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
