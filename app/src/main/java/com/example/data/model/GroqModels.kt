package com.example.data.model

data class GroqModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val contextWindow: String,
    val speedRating: String, // e.g., "Ultra Fast (~800 tps)", "Fast (~300 tps)"
    val isRecommended: Boolean = false
)

object GroqModels {
    val AVAILABLE_MODELS = listOf(
        GroqModelInfo(
            id = "llama-3.3-70b-versatile",
            name = "Llama 3.3 70B Versatile",
            description = "State-of-the-art intelligence, ideal for complex assistant workflows and reasoning.",
            contextWindow = "128k tokens",
            speedRating = "Fast (~280 t/s)",
            isRecommended = true
        ),
        GroqModelInfo(
            id = "llama-3.1-8b-instant",
            name = "Llama 3.1 8B Instant",
            description = "Near-instantaneous inference speed, perfect for rapid hands-free voice interactions.",
            contextWindow = "128k tokens",
            speedRating = "Ultra Fast (~850 t/s)"
        ),
        GroqModelInfo(
            id = "mixtral-8x7b-32768",
            name = "Mixtral 8x7B MoE",
            description = "Mixture of Experts architecture for high-efficiency multi-domain problem solving.",
            contextWindow = "32k tokens",
            speedRating = "Ultra Fast (~500 t/s)"
        ),
        GroqModelInfo(
            id = "gemma2-9b-it",
            name = "Gemma 2 9B IT",
            description = "Google instruction-tuned model running at ultra-high throughput on Groq LPU.",
            contextWindow = "8k tokens",
            speedRating = "Ultra Fast (~550 t/s)"
        )
    )

    const val DEFAULT_MODEL = "llama-3.3-70b-versatile"
}
