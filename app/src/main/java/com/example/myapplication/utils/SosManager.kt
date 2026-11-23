package com.example.myapplication.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.PreferencesManager

object SosManager {

    private const val ALERT_CHANNEL_ID = "distress_sent_channel"
    private const val ALERT_NOTIFICATION_ID = 2002

    fun triggerSosAlert(context: Context, onComplete: ((Boolean, String) -> Unit)? = null) {
        val prefs = PreferencesManager(context)
        val contacts = prefs.getContacts()

        if (contacts.isEmpty()) {
            val msg = "No emergency contacts saved! Please add contacts in the Contacts tab."
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            onComplete?.invoke(false, msg)
            return
        }

        // Vibrate to alert user that SOS has been triggered
        vibrateDevice(context)

        Toast.makeText(context, "Fetching location for SOS Alert...", Toast.LENGTH_SHORT).show()

        LocationHelper.getCurrentLocation(context) { location ->
            val locationUrl = if (location != null) {
                val url = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                prefs.saveLastLocation(location.latitude, location.longitude)
                url
            } else {
                prefs.getLastLocationUrl() ?: "Location unavailable"
            }

            val template = prefs.getSosMessage()
            val finalMessage = if (template.contains("{LOCATION}")) {
                template.replace("{LOCATION}", locationUrl)
            } else {
                "$template\nLocation: $locationUrl"
            }

            // Check direct SMS sending permission
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasSmsPermission) {
                val errorMsg = "🚨 Automatic SOS blocked: SMS permission not granted! Please allow SMS in App Info."
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onComplete?.invoke(false, errorMsg)
                return@getCurrentLocation
            }

            // Retrieve SmsManager in a modern, forward-compatible way (Android 12+ / API 31+ compatible)
            val smsManager: SmsManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
                    ?: @Suppress("DEPRECATION") SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            if (smsManager == null) {
                val errorMsg = "🚨 SOS Error: Telephony/SMS service is unavailable on this device."
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onComplete?.invoke(false, errorMsg)
                return@getCurrentLocation
            }

            var successCount = 0
            for (contact in contacts) {
                try {
                    val parts = smsManager.divideMessage(finalMessage)
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(
                            contact.phoneNumber,
                            null,
                            parts,
                            null,
                            null
                        )
                    } else {
                        smsManager.sendTextMessage(
                            contact.phoneNumber,
                            null,
                            finalMessage,
                            null,
                            null
                        )
                    }
                    successCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (successCount > 0) {
                val statusMsg = "🚨 SOS Alert sent automatically to $successCount contact(s)!"
                Toast.makeText(context, statusMsg, Toast.LENGTH_LONG).show()
                showDistressSentNotification(context, successCount, finalMessage)
                onComplete?.invoke(true, statusMsg)
            } else {
                val errorMsg = "🚨 Failed to send SOS SMS. Please check your SIM network."
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    private fun showDistressSentNotification(context: Context, contactCount: Int, message: String) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    ALERT_CHANNEL_ID,
                    "Distress Alert Sent Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Confirms when an emergency distress message has been dispatched"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sos)
                .setContentTitle("🚨 Emergency Distress Message Sent")
                .setContentText("Distress alert with live location sent to $contactCount contact(s).")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("🚨 Distress message with your live location was successfully sent to $contactCount emergency contact(s):\n\n$message")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrateDevice(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(
                    VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(
                    VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
