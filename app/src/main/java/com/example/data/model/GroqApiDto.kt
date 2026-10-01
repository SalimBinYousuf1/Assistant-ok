package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String,
    val messages: List<MessageDto>,
    val temperature: Float = 0.6f,
    @Json(name = "max_tokens") val maxTokens: Int = 1024,
    val tools: List<ToolDto>? = null,
    @Json(name = "tool_choice") val toolChoice: String? = null
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    val role: String,
    val content: String? = null,
    @Json(name = "tool_calls") val toolCalls: List<ToolCallDto>? = null,
    @Json(name = "tool_call_id") val toolCallId: String? = null,
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class ToolDto(
    val type: String = "function",
    val function: FunctionDefDto
)

@JsonClass(generateAdapter = true)
data class FunctionDefDto(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val id: String?,
    val choices: List<ChoiceDto>?,
    val usage: UsageDto?
)

@JsonClass(generateAdapter = true)
data class ChoiceDto(
    val index: Int?,
    val message: MessageDto?,
    @Json(name = "finish_reason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class ToolCallDto(
    val id: String,
    val type: String = "function",
    val function: FunctionCallDto
)

@JsonClass(generateAdapter = true)
data class FunctionCallDto(
    val name: String,
    val arguments: String
)

@JsonClass(generateAdapter = true)
data class UsageDto(
    @Json(name = "prompt_tokens") val promptTokens: Int?,
    @Json(name = "completion_tokens") val completionTokens: Int?,
    @Json(name = "total_tokens") val totalTokens: Int?,
    @Json(name = "total_time") val totalTime: Double?
)

@JsonClass(generateAdapter = true)
data class ModelsListResponse(
    val data: List<ModelEntryDto>?
)

@JsonClass(generateAdapter = true)
data class ModelEntryDto(
    val id: String,
    val owned_by: String? = null
)
