package com.example.data.api

import com.example.data.model.ChatCompletionRequest
import com.example.data.model.ChatCompletionResponse
import com.example.data.model.ModelsListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @GET("openai/v1/models")
    suspend fun listModels(
        @Header("Authorization") authorization: String
    ): Response<ModelsListResponse>
}
