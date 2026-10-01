package com.example.data.repository

import android.util.Log
import com.example.data.api.GroqClient
import com.example.data.local.ChatMessageDao
import com.example.data.local.SettingsManager
import com.example.data.model.ChatCompletionRequest
import com.example.data.model.ChatMessage
import com.example.data.model.MessageDto
import com.example.data.tools.ActionResult
import com.example.data.tools.SystemActionExecutor
import com.example.data.tools.ToolDefinitionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class AssistantResponseResult {
    data class Success(
        val assistantMessage: ChatMessage,
        val executedAction: ActionResult? = null
    ) : AssistantResponseResult()

    data class Error(
        val userFriendlyMessage: String,
        val technicalDetails: String? = null
    ) : AssistantResponseResult()
}

class AssistantRepository(
    private val chatMessageDao: ChatMessageDao,
    private val settingsManager: SettingsManager,
    private val actionExecutor: SystemActionExecutor
) {

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessagesAsc()

    suspend fun insertMessage(message: ChatMessage): Long = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        chatMessageDao.deleteMessageById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        chatMessageDao.clearAll()
    }

    suspend fun testApiKey(apiKey: String): Result<Long> = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be blank"))
        }

        try {
            val startTime = System.currentTimeMillis()
            val response = GroqClient.groqService.listModels("Bearer $key")
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                Result.success(latency)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                val errorMsg = try {
                    val json = JSONObject(errorBody)
                    json.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code()}"
                } catch (e: Exception) {
                    "HTTP ${response.code()}: $errorBody"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processUserPrompt(
        prompt: String,
        isVoice: Boolean
    ): AssistantResponseResult = withContext(Dispatchers.IO) {
        val apiKey = settingsManager.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext AssistantResponseResult.Error(
                userFriendlyMessage = "Groq API key not configured. Tap Settings to enter your Groq API key."
            )
        }

        val userMessage = ChatMessage(
            role = "user",
            content = prompt.trim(),
            timestamp = System.currentTimeMillis(),
            isVoice = isVoice
        )
        insertMessage(userMessage)

        val selectedModel = settingsManager.getSelectedModel()
        val systemPrompt = settingsManager.getSystemPrompt()

        // Fetch recent conversation history for context (up to 8 messages)
        val recentDbMessages = chatMessageDao.getRecentMessagesList(8).reversed()

        val apiMessages = mutableListOf<MessageDto>()
        apiMessages.add(
            MessageDto(role = "system", content = systemPrompt)
        )

        for (msg in recentDbMessages) {
            if (msg.role == "user" || msg.role == "assistant") {
                apiMessages.add(
                    MessageDto(
                        role = msg.role,
                        content = msg.content
                    )
                )
            }
        }

        val tools = ToolDefinitionProvider.getAssistantTools()
        val request = ChatCompletionRequest(
            model = selectedModel,
            messages = apiMessages,
            temperature = 0.5f,
            maxTokens = 800,
            tools = tools,
            toolChoice = "auto"
        )

        try {
            val response = GroqClient.groqService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: ""
                val parsedError = parseApiError(response.code(), errorBody)
                val errorMessage = ChatMessage(
                    role = "assistant",
                    content = parsedError,
                    modelUsed = selectedModel,
                    isVoice = false,
                    isError = true
                )
                insertMessage(errorMessage)
                return@withContext AssistantResponseResult.Error(parsedError, errorBody)
            }

            val body = response.body()
            val firstChoice = body?.choices?.firstOrNull()
            val responseMessage = firstChoice?.message

            if (responseMessage == null) {
                val errText = "Empty response received from Groq."
                val errorMsg = ChatMessage(
                    role = "assistant",
                    content = errText,
                    modelUsed = selectedModel,
                    isError = true
                )
                insertMessage(errorMsg)
                return@withContext AssistantResponseResult.Error(errText)
            }

            val toolCalls = responseMessage.toolCalls
            if (!toolCalls.isNullOrEmpty()) {
                val toolCall = toolCalls.first()
                val functionName = toolCall.function.name
                val functionArgs = toolCall.function.arguments

                Log.d("AssistantRepo", "Executing tool: $functionName args: $functionArgs")
                val actionResult = actionExecutor.execute(functionName, functionArgs)

                val spokenSummary = if (actionResult.success) {
                    actionResult.summary
                } else {
                    "${actionResult.summary}. Let me know if you'd like to try again."
                }

                val assistantMessage = ChatMessage(
                    role = "assistant",
                    content = spokenSummary,
                    timestamp = System.currentTimeMillis(),
                    modelUsed = selectedModel,
                    isVoice = isVoice,
                    actionType = actionResult.toolType,
                    actionSummary = actionResult.summary,
                    actionPayload = actionResult.payload ?: actionResult.detail,
                    isError = !actionResult.success
                )
                insertMessage(assistantMessage)
                return@withContext AssistantResponseResult.Success(
                    assistantMessage = assistantMessage,
                    executedAction = actionResult
                )
            } else {
                val assistantText = responseMessage.content?.trim() ?: "No response generated."
                val assistantMessage = ChatMessage(
                    role = "assistant",
                    content = assistantText,
                    timestamp = System.currentTimeMillis(),
                    modelUsed = selectedModel,
                    isVoice = isVoice
                )
                insertMessage(assistantMessage)
                return@withContext AssistantResponseResult.Success(
                    assistantMessage = assistantMessage
                )
            }
        } catch (e: Exception) {
            Log.e("AssistantRepo", "Network or processing error", e)
            val friendlyError = when {
                e is java.net.UnknownHostException -> "No internet connection. Please verify your Wi-Fi or cellular data."
                e is java.net.SocketTimeoutException -> "Request timed out waiting for Groq. Please try again."
                else -> "Connection error: ${e.localizedMessage ?: "Unknown failure"}"
            }
            val errorMsg = ChatMessage(
                role = "assistant",
                content = friendlyError,
                modelUsed = selectedModel,
                isError = true
            )
            insertMessage(errorMsg)
            return@withContext AssistantResponseResult.Error(friendlyError, e.message)
        }
    }

    private fun parseApiError(code: Int, errorBody: String): String {
        return when (code) {
            401 -> "Invalid Groq API Key. Please verify your key in Settings."
            429 -> "Groq rate limit exceeded. Please wait a few moments and try again."
            400 -> {
                val msg = try {
                    JSONObject(errorBody).optJSONObject("error")?.optString("message")
                } catch (e: Exception) { null }
                msg ?: "Bad request to Groq API (HTTP 400)."
            }
            500, 502, 503 -> "Groq service is temporarily unavailable. Please retry in a few moments."
            else -> "Groq API error ($code): ${errorBody.take(120)}"
        }
    }
}
