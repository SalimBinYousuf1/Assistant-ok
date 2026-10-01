package com.example.data.model

data class GroqModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val contextWindow: String,
    val speedRating: String,
    val isRecommended: Boolean = false
)

object GroqModels {
    val AVAILABLE_MODELS = listOf(
        GroqModelInfo(
            id = "openai/gpt-oss-120b",
            name = "GPT-OSS 120B",
            description = "Flagship 120B open model by OpenAI for high-precision reasoning, multi-step planning, and agent autonomy.",
            contextWindow = "128k tokens",
            speedRating = "Fast (~250 t/s)",
            isRecommended = true
        ),
        GroqModelInfo(
            id = "openai/gpt-oss-20b",
            name = "GPT-OSS 20B",
            description = "Ultra-responsive 20B open model by OpenAI built for instant voice commands and rapid execution.",
            contextWindow = "128k tokens",
            speedRating = "Ultra Fast (~750 t/s)"
        ),
        GroqModelInfo(
            id = "groq/compound",
            name = "Groq Compound",
            description = "Groq compound agent system tailored for native Android tool execution, system gestures, and multi-turn workflows.",
            contextWindow = "128k tokens",
            speedRating = "Ultra Fast (~800 t/s)"
        ),
        GroqModelInfo(
            id = "qwen/qwen3.8-27b",
            name = "Qwen 3.8 27B",
            description = "Advanced 27B reasoning model with exceptional multilingual comprehension, logic, and mathematics.",
            contextWindow = "64k tokens",
            speedRating = "Ultra Fast (~450 t/s)"
        )
    )

    const val DEFAULT_MODEL = "openai/gpt-oss-120b"
}
