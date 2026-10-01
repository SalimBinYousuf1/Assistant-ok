package com.example.data.system

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import com.example.service.SalimAccessibilityService

object PermissionManager {

    fun isMicrophoneGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isNotificationsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (SalimAccessibilityService.isRunning()) return true
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = "${context.packageName}/${SalimAccessibilityService::class.java.canonicalName}"
        val simpleName = SalimAccessibilityService::class.java.simpleName
        return enabledServices.any { service ->
            service.id.contains(simpleName) || service.id.equals(expectedServiceName, ignoreCase = true)
        }
    }

    fun isDefaultAssistant(context: Context): Boolean {
        return try {
            val defaultAssist = Settings.Secure.getString(context.contentResolver, "assistant")
            defaultAssist != null && defaultAssist.contains(context.packageName)
        } catch (e: Exception) {
            false
        }
    }
}
