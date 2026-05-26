package com.example.organizadorapps

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object QuickSettingsNavigator {

    fun openWifi(context: Context) {
        open(context, Intent(Settings.ACTION_WIFI_SETTINGS), "No se pudo abrir Wi‑Fi")
    }

    fun openBluetooth(context: Context) {
        val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Settings.ACTION_BLUETOOTH_SETTINGS
        } else {
            Settings.ACTION_BLUETOOTH_SETTINGS
        }
        open(context, Intent(action), "No se pudo abrir Bluetooth")
    }

    fun openMobileData(context: Context) {
        val intent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
        val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        open(context, intent, "No se pudo abrir Datos móviles", fallback)
    }

    private fun open(
        context: Context,
        intent: Intent,
        errorMessage: String,
        fallback: Intent? = null
    ) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            if (fallback != null) {
                try {
                    context.startActivity(fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    return
                } catch (_: Exception) {
                    // ignore and show toast below
                }
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
