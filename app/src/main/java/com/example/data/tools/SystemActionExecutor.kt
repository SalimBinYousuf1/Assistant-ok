package com.example.data.tools

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.util.Log
import com.example.data.api.GroqClient
import com.example.data.model.FormattedWeatherData
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActionResult(
    val success: Boolean,
    val summary: String,
    val toolType: String,
    val detail: String? = null,
    val payload: String? = null
)

class SystemActionExecutor(private val context: Context) {

    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    suspend fun execute(toolName: String, argumentsJson: String): ActionResult {
        Log.d("SystemActionExecutor", "Executing tool: $toolName with args: $argumentsJson")
        return try {
            val args = if (argumentsJson.isNotBlank()) JSONObject(argumentsJson) else JSONObject()
            when (toolName) {
                "set_alarm" -> executeSetAlarm(args)
                "set_timer" -> executeSetTimer(args)
                "toggle_flashlight" -> executeToggleFlashlight(args)
                "open_app" -> executeOpenApp(args)
                "get_device_status" -> executeGetDeviceStatus()
                "get_weather" -> executeGetWeather(args)
                "search_web" -> executeSearchWeb(args)
                "make_phone_call" -> executeMakePhoneCall(args)
                "send_message" -> executeSendMessage(args)
                else -> ActionResult(
                    success = false,
                    summary = "Unknown tool requested: $toolName",
                    toolType = toolName
                )
            }
        } catch (e: Exception) {
            Log.e("SystemActionExecutor", "Error executing $toolName", e)
            ActionResult(
                success = false,
                summary = "Failed to perform $toolName: ${e.localizedMessage ?: "Unknown error"}",
                toolType = toolName,
                detail = e.message
            )
        }
    }

    private fun executeSetAlarm(args: JSONObject): ActionResult {
        val hour = args.optInt("hour", -1)
        val minute = args.optInt("minute", 0)
        val message = args.optString("message", "Salim Alarm")

        if (hour !in 0..23 || minute !in 0..59) {
            return ActionResult(
                success = false,
                summary = "Invalid alarm time provided: $hour:$minute",
                toolType = "ALARM"
            )
        }

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            ActionResult(
                success = true,
                summary = "Alarm set for $formattedTime ('$message')",
                toolType = "ALARM",
                detail = "Alarm dispatched to system clock.",
                payload = "$hour:$minute"
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                summary = "Clock app not available or cannot set alarm.",
                toolType = "ALARM",
                detail = e.message
            )
        }
    }

    private fun executeSetTimer(args: JSONObject): ActionResult {
        val seconds = args.optInt("seconds", -1)
        val message = args.optString("message", "Salim Timer")

        if (seconds <= 0) {
            return ActionResult(
                success = false,
                summary = "Invalid timer duration ($seconds seconds).",
                toolType = "TIMER"
            )
        }

        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            val minutes = seconds / 60
            val remSeconds = seconds % 60
            val timeDesc = if (minutes > 0) "${minutes}m ${remSeconds}s" else "${seconds}s"
            ActionResult(
                success = true,
                summary = "Timer set for $timeDesc ('$message')",
                toolType = "TIMER",
                detail = "Timer running in Android clock.",
                payload = seconds.toString()
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                summary = "Clock app not available to set timer.",
                toolType = "TIMER",
                detail = e.message
            )
        }
    }

    private fun executeToggleFlashlight(args: JSONObject): ActionResult {
        val turnOn = args.optBoolean("turn_on", true)
        val cm = cameraManager
            ?: return ActionResult(
                success = false,
                summary = "Camera manager is not supported on this device.",
                toolType = "FLASHLIGHT"
            )

        return try {
            var torchFound = false
            for (cameraId in cm.cameraIdList) {
                val characteristics = cm.getCameraCharacteristics(cameraId)
                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (flashAvailable) {
                    cm.setTorchMode(cameraId, turnOn)
                    torchFound = true
                    break
                }
            }

            if (torchFound) {
                val stateText = if (turnOn) "ON" else "OFF"
                ActionResult(
                    success = true,
                    summary = "Flashlight turned $stateText",
                    toolType = "FLASHLIGHT",
                    payload = if (turnOn) "ON" else "OFF"
                )
            } else {
                ActionResult(
                    success = false,
                    summary = "No camera with flash/torch found on device.",
                    toolType = "FLASHLIGHT"
                )
            }
        } catch (e: CameraAccessException) {
            ActionResult(
                success = false,
                summary = "Could not toggle flashlight: ${e.message}",
                toolType = "FLASHLIGHT",
                detail = e.message
            )
        }
    }

    private fun executeOpenApp(args: JSONObject): ActionResult {
        val targetName = args.optString("app_name", "").trim()
        if (targetName.isBlank()) {
            return ActionResult(
                success = false,
                summary = "No application name provided.",
                toolType = "APP_LAUNCH"
            )
        }

        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
        val lowerTarget = targetName.lowercase(Locale.getDefault())

        // Find exact or closest match
        val matchedApp = resolvedApps.firstOrNull { resolveInfo ->
            val label = resolveInfo.loadLabel(pm).toString().lowercase(Locale.getDefault())
            label == lowerTarget || label.contains(lowerTarget) || resolveInfo.activityInfo.packageName.lowercase(Locale.getDefault()).contains(lowerTarget)
        }

        return if (matchedApp != null) {
            val packageName = matchedApp.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                val appTitle = matchedApp.loadLabel(pm).toString()
                ActionResult(
                    success = true,
                    summary = "Opening $appTitle",
                    toolType = "APP_LAUNCH",
                    payload = packageName
                )
            } else {
                ActionResult(
                    success = false,
                    summary = "Cannot launch $targetName (no launcher intent).",
                    toolType = "APP_LAUNCH"
                )
            }
        } else {
            ActionResult(
                success = false,
                summary = "App '$targetName' not found on this device.",
                toolType = "APP_LAUNCH"
            )
        }
    }

    private fun executeGetDeviceStatus(): ActionResult {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val currentTime = SimpleDateFormat("h:mm a, EEEE, MMM d", Locale.getDefault()).format(Date())
        val chargeStatusText = if (isCharging) "Charging" else "Not charging"

        val summary = "Battery: $batteryPct% ($chargeStatusText). Current time: $currentTime"
        return ActionResult(
            success = true,
            summary = summary,
            toolType = "DEVICE_STATUS",
            detail = "Device status read from system services.",
            payload = "$batteryPct%"
        )
    }

    private suspend fun executeGetWeather(args: JSONObject): ActionResult {
        val locationQuery = args.optString("location", "London").trim()
        val geoResponse = GroqClient.geocodingService.searchCity(name = locationQuery, count = 1)
        val firstResult = geoResponse.body()?.results?.firstOrNull()
            ?: return ActionResult(
                success = false,
                summary = "Could not locate coordinates for '$locationQuery'.",
                toolType = "WEATHER"
            )

        val weatherResponse = GroqClient.meteoService.getCurrentWeather(
            latitude = firstResult.latitude,
            longitude = firstResult.longitude,
            currentWeather = true
        )

        val current = weatherResponse.body()?.currentWeather
            ?: return ActionResult(
                success = false,
                summary = "Weather data unavailable for ${firstResult.name}.",
                toolType = "WEATHER"
            )

        val condition = parseWmoCode(current.weathercode)
        val tempC = current.temperature
        val tempF = (tempC * 9.0 / 5.0) + 32.0

        val summary = "Weather in ${firstResult.name}, ${firstResult.country ?: ""}: ${String.format(Locale.getDefault(), "%.1f°C / %.1f°F", tempC, tempF)}, $condition. Wind: ${current.windspeed} km/h"
        return ActionResult(
            success = true,
            summary = summary,
            toolType = "WEATHER",
            detail = "Live data from Open-Meteo",
            payload = "${current.temperature}°C"
        )
    }

    private fun parseWmoCode(code: Int): String {
        return when (code) {
            0 -> "Clear sky ☀️"
            1, 2, 3 -> "Mainly clear to overcast ⛅"
            45, 48 -> "Fog and depositing rime fog 🌫️"
            51, 53, 55 -> "Drizzle 🌦️"
            61, 63, 65 -> "Rain 🌧️"
            71, 73, 75 -> "Snow fall ❄️"
            77 -> "Snow grains ❄️"
            80, 81, 82 -> "Rain showers 🌧️"
            85, 86 -> "Snow showers 🌨️"
            95 -> "Thunderstorm ⛈️"
            96, 99 -> "Thunderstorm with hail ⛈️"
            else -> "Partly cloudy 🌤️"
        }
    }

    private fun executeSearchWeb(args: JSONObject): ActionResult {
        val query = args.optString("query", "").trim()
        if (query.isBlank()) {
            return ActionResult(
                success = false,
                summary = "Empty search query.",
                toolType = "SEARCH"
            )
        }

        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(
                success = true,
                summary = "Searching web for '$query'",
                toolType = "SEARCH",
                payload = query
            )
        } catch (e: Exception) {
            // Fallback to web browser search URL
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                ActionResult(
                    success = true,
                    summary = "Searching web for '$query'",
                    toolType = "SEARCH",
                    payload = query
                )
            } catch (err: Exception) {
                ActionResult(
                    success = false,
                    summary = "No browser available to search: ${err.message}",
                    toolType = "SEARCH"
                )
            }
        }
    }

    private fun executeMakePhoneCall(args: JSONObject): ActionResult {
        val phoneNumber = args.optString("phone_number", "").trim()
        if (phoneNumber.isBlank()) {
            return ActionResult(
                success = false,
                summary = "No phone number specified.",
                toolType = "PHONE_CALL"
            )
        }

        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(dialIntent)
            ActionResult(
                success = true,
                summary = "Dialing $phoneNumber",
                toolType = "PHONE_CALL",
                payload = phoneNumber
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                summary = "Unable to open phone dialer: ${e.message}",
                toolType = "PHONE_CALL"
            )
        }
    }

    private fun executeSendMessage(args: JSONObject): ActionResult {
        val phoneNumber = args.optString("phone_number", "").trim()
        val message = args.optString("message", "").trim()

        val uri = if (phoneNumber.isNotBlank()) {
            Uri.parse("smsto:${Uri.encode(phoneNumber)}")
        } else {
            Uri.parse("smsto:")
        }

        val sendIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(sendIntent)
            ActionResult(
                success = true,
                summary = "Opening message composer to send: '$message'",
                toolType = "MESSAGE",
                payload = message
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                summary = "No SMS application found: ${e.message}",
                toolType = "MESSAGE"
            )
        }
    }
}
