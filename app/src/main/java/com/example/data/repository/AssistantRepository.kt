package com.example.data.repository

import android.util.Log
import com.example.data.api.GroqClient
import com.example.data.local.ChatMessageDao
import com.example.data.local.SettingsManager
import com.example.data.model.AgentStep
import com.example.data.model.ChatCompletionRequest
import com.example.data.model.ChatMessage
import com.example.data.model.MessageDto
import com.example.data.tools.ActionResult
import com.example.data.tools.SystemActionExecutor
import com.example.data.tools.ToolDefinitionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class AssistantResponseResult {
    data class Success(
        val assistantMessage: ChatMessage,
        val steps: List<AgentStep> = emptyList(),
        val lastExecutedAction: ActionResult? = null
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
        isVoice: Boolean,
        onStepUpdate: ((AgentStep) -> Unit)? = null
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

        // Maintain conversation context (up to last 10 messages)
        val recentDbMessages = chatMessageDao.getRecentMessagesList(10).reversed()

        val apiMessages = mutableListOf<MessageDto>()
        apiMessages.add(MessageDto(role = "system", content = systemPrompt))

        for (msg in recentDbMessages) {
            if (msg.role == "user" || msg.role == "assistant") {
                apiMessages.add(MessageDto(role = msg.role, content = msg.content))
            }
        }

        val tools = ToolDefinitionProvider.getAssistantTools()
        val executedSteps = mutableListOf<AgentStep>()
        var lastActionResult: ActionResult? = null
        var finalAssistantText = ""
        var stepCounter = 1
        val maxIterations = 5

        for (iteration in 0 until maxIterations) {
            val request = ChatCompletionRequest(
                model = selectedModel,
                messages = apiMessages,
                temperature = 0.4f,
                maxTokens = 850,
                tools = tools,
                toolChoice = "auto"
            )

            val response = try {
                GroqClient.groqService.createChatCompletion(
                    authorization = "Bearer $apiKey",
                    request = request
                )
            } catch (e: Exception) {
                Log.e("AssistantRepo", "Network error calling Groq", e)
                val friendly = when {
                    e is java.net.UnknownHostException -> "No internet connection. Please verify your connection."
                    e is java.net.SocketTimeoutException -> "Request timed out waiting for Groq."
                    else -> "Connection error: ${e.localizedMessage ?: "Unknown failure"}"
                }
                val errMessage = ChatMessage(
                    role = "assistant",
                    content = friendly,
                    modelUsed = selectedModel,
                    isError = true
                )
                insertMessage(errMessage)
                return@withContext AssistantResponseResult.Error(friendly, e.message)
            }

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: ""
                val parsedError = parseApiError(response.code(), errorBody)
                val errMessage = ChatMessage(
                    role = "assistant",
                    content = parsedError,
                    modelUsed = selectedModel,
                    isError = true
                )
                insertMessage(errMessage)
                return@withContext AssistantResponseResult.Error(parsedError, errorBody)
            }

            val body = response.body()
            val choice = body?.choices?.firstOrNull()
            val messageDto = choice?.message

            if (messageDto == null) {
                break
            }

            val toolCalls = messageDto.toolCalls
            if (!toolCalls.isNullOrEmpty()) {
                // Agent planned tool execution step(s)
                apiMessages.add(messageDto)

                for (toolCall in toolCalls) {
                    val functionName = toolCall.function.name
                    val functionArgs = toolCall.function.arguments

                    val stepTitle = formatStepTitle(functionName, functionArgs)
                    val activeStep = AgentStep(
                        stepIndex = stepCounter++,
                        title = stepTitle,
                        toolName = functionName,
                        status = "EXECUTING"
                    )
                    onStepUpdate?.invoke(activeStep)

                    // Execute tool via Android APIs & Accessibility fallback
                    val actionResult = actionExecutor.execute(functionName, functionArgs)
                    lastActionResult = actionResult

                    val completedStep = activeStep.copy(
                        status = if (actionResult.success) "SUCCESS" else "FAILED",
                        observation = actionResult.summary
                    )
                    executedSteps.add(completedStep)
                    onStepUpdate?.invoke(completedStep)

                    // Feed tool observation back to model
                    apiMessages.add(
                        MessageDto(
                            role = "tool",
                            toolCallId = toolCall.id,
                            name = functionName,
                            content = actionResult.summary + (if (actionResult.detail != null) " (${actionResult.detail})" else "")
                        )
                    )
                }
                // Next iteration allows the agent to observe results, recover from failure, or finalize
            } else {
                // Agent produced final response text
                finalAssistantText = messageDto.content?.trim() ?: ""
                break
            }
        }

        // If no explicit text was produced, synthesize from executed steps
        if (finalAssistantText.isBlank()) {
            finalAssistantText = if (executedSteps.isNotEmpty()) {
                executedSteps.joinToString("\n") { "• ${it.observation ?: it.title}" }
            } else {
                "Task completed."
            }
        }

        val stepsJson = if (executedSteps.isNotEmpty()) {
            val jsonArr = JSONArray()
            for (step in executedSteps) {
                val obj = JSONObject().apply {
                    put("stepIndex", step.stepIndex)
                    put("title", step.title)
                    put("toolName", step.toolName)
                    put("status", step.status)
                    put("observation", step.observation)
                }
                jsonArr.put(obj)
            }
            jsonArr.toString()
        } else null

        val assistantMessage = ChatMessage(
            role = "assistant",
            content = finalAssistantText,
            timestamp = System.currentTimeMillis(),
            modelUsed = selectedModel,
            isVoice = isVoice,
            actionType = lastActionResult?.toolType,
            actionSummary = lastActionResult?.summary,
            actionPayload = lastActionResult?.payload ?: lastActionResult?.detail,
            stepsJson = stepsJson,
            isError = lastActionResult?.success == false && executedSteps.all { it.status == "FAILED" }
        )
        insertMessage(assistantMessage)

        return@withContext AssistantResponseResult.Success(
            assistantMessage = assistantMessage,
            steps = executedSteps,
            lastExecutedAction = lastActionResult
        )
    }

    private fun formatStepTitle(toolName: String, argsJson: String): String {
        return try {
            val args = if (argsJson.isNotBlank()) JSONObject(argsJson) else JSONObject()
            when (toolName) {
                "set_alarm" -> "Setting alarm for ${args.optInt("hour")}:${String.format("%02d", args.optInt("minute"))}"
                "set_timer" -> "Setting ${args.optInt("seconds")}s countdown timer"
                "toggle_flashlight" -> if (args.optBoolean("turn_on", true)) "Turning on flashlight" else "Turning off flashlight"
                "open_app" -> "Opening ${args.optString("app_name")}"
                "get_device_status" -> "Checking device status"
                "get_weather" -> "Checking live weather for ${args.optString("location")}"
                "search_web" -> "Searching web for '${args.optString("query")}'"
                "perform_system_gesture" -> "Executing system gesture: ${args.optString("gesture").uppercase()}"
                "open_settings_page" -> "Opening ${args.optString("settings_type").replaceFirstChar { it.uppercase() }} Settings"
                "create_calendar_event" -> "Scheduling '${args.optString("title")}'"
                "calculate_math" -> "Computing ${args.optString("expression")}"
                "click_ui_element" -> "Clicking screen element '${args.optString("target_text")}'"
                else -> "Running $toolName"
            }
        } catch (e: Exception) {
            "Running $toolName"
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
