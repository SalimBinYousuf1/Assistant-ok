package com.example.data.tools

import com.example.data.model.FunctionDefDto
import com.example.data.model.ToolDto

object ToolDefinitionProvider {

    fun getAssistantTools(): List<ToolDto> {
        return listOf(
            ToolDto(
                function = FunctionDefDto(
                    name = "set_alarm",
                    description = "Sets an alarm on the user's Android device at the specified hour and minute.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "hour" to mapOf(
                                "type" to "integer",
                                "description" to "The hour of the alarm in 24-hour format (0-23)."
                            ),
                            "minute" to mapOf(
                                "type" to "integer",
                                "description" to "The minute of the alarm (0-59)."
                            ),
                            "message" to mapOf(
                                "type" to "string",
                                "description" to "Optional label or description for the alarm."
                            )
                        ),
                        "required" to listOf("hour", "minute")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "set_timer",
                    description = "Sets a countdown timer on the user's Android device for the given number of seconds.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "seconds" to mapOf(
                                "type" to "integer",
                                "description" to "The duration of the timer in seconds (e.g., 300 for 5 minutes)."
                            ),
                            "message" to mapOf(
                                "type" to "string",
                                "description" to "Optional label for the timer."
                            )
                        ),
                        "required" to listOf("seconds")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "toggle_flashlight",
                    description = "Turns the device camera flashlight / torch on or off.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "turn_on" to mapOf(
                                "type" to "boolean",
                                "description" to "True to turn on the flashlight, false to turn it off."
                            )
                        ),
                        "required" to listOf("turn_on")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "open_app",
                    description = "Finds and opens an installed application on the user's Android phone by app name.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "app_name" to mapOf(
                                "type" to "string",
                                "description" to "The name of the app to launch (e.g. YouTube, Spotify, WhatsApp, Maps, Camera, Calculator, Settings)."
                            )
                        ),
                        "required" to listOf("app_name")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "get_device_status",
                    description = "Retrieves real-time Android device status including battery level, charging status, and system time.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to emptyMap<String, Any>()
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "get_weather",
                    description = "Gets live current weather and temperature for any city or location around the world.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "location" to mapOf(
                                "type" to "string",
                                "description" to "The city or location name (e.g., London, Tokyo, New York, Cairo)."
                            )
                        ),
                        "required" to listOf("location")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "search_web",
                    description = "Performs a web search on the device for queries that need external web browsing or search results.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "query" to mapOf(
                                "type" to "string",
                                "description" to "The search query string."
                            )
                        ),
                        "required" to listOf("query")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "make_phone_call",
                    description = "Opens the device dialer ready to call a phone number.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "phone_number" to mapOf(
                                "type" to "string",
                                "description" to "The phone number to dial."
                            )
                        ),
                        "required" to listOf("phone_number")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "send_message",
                    description = "Opens the SMS/messaging app to compose a text message.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "phone_number" to mapOf(
                                "type" to "string",
                                "description" to "Optional recipient phone number."
                            ),
                            "message" to mapOf(
                                "type" to "string",
                                "description" to "The text message body."
                            )
                        ),
                        "required" to listOf("message")
                    )
                )
            )
        )
    }
}
