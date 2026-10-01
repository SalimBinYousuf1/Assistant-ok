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
                                "description" to "The duration of the timer in seconds (e.g., 300 for 5 minutes, 600 for 10 minutes)."
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
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "perform_system_gesture",
                    description = "Performs an autonomous Android navigation gesture such as going Home, Back, opening Recents, pulling down Notifications, or Quick Settings.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "gesture" to mapOf(
                                "type" to "string",
                                "enum" to listOf("home", "back", "recents", "notifications", "quick_settings", "lock_screen"),
                                "description" to "The system gesture to execute."
                            )
                        ),
                        "required" to listOf("gesture")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "open_settings_page",
                    description = "Opens a specific Android System Settings panel directly (Wi-Fi, Bluetooth, Battery, Display, Apps, Sound).",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "settings_type" to mapOf(
                                "type" to "string",
                                "enum" to listOf("wifi", "bluetooth", "battery", "display", "sound", "apps", "date", "general"),
                                "description" to "The type of settings page to launch."
                            )
                        ),
                        "required" to listOf("settings_type")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "create_calendar_event",
                    description = "Creates a calendar event or reminder on the user's Android calendar.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "title" to mapOf(
                                "type" to "string",
                                "description" to "Event title or summary."
                            ),
                            "description" to mapOf(
                                "type" to "string",
                                "description" to "Optional event details or notes."
                            ),
                            "minutes_from_now" to mapOf(
                                "type" to "integer",
                                "description" to "How many minutes from now the event should start (default 60)."
                            )
                        ),
                        "required" to listOf("title")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "calculate_math",
                    description = "Computes mathematical expressions and arithmetic calculations with precision.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "expression" to mapOf(
                                "type" to "string",
                                "description" to "The mathematical expression to evaluate (e.g., '145 * 0.18', '2^8', 'sqrt(144)')."
                            )
                        ),
                        "required" to listOf("expression")
                    )
                )
            ),
            ToolDto(
                function = FunctionDefDto(
                    name = "click_ui_element",
                    description = "Autonomous accessibility fallback: finds and clicks a button or interactive UI control on screen matching the given text label.",
                    parameters = mapOf(
                        "type" to "object",
                        "properties" to mapOf(
                            "target_text" to mapOf(
                                "type" to "string",
                                "description" to "The exact or partial text of the button or item on screen to click."
                            )
                        ),
                        "required" to listOf("target_text")
                    )
                )
            )
        )
    }
}
