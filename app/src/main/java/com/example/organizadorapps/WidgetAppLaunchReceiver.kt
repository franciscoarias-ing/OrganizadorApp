package com.example.organizadorapps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class WidgetAppLaunchReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty()
        val appName = intent.getStringExtra(EXTRA_APP_NAME).orEmpty().ifBlank { "la app" }

        if (packageName.isBlank()) return

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "No se pudo abrir $appName", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ACTION_OPEN_APP = "com.example.organizadorapps.widget.OPEN_APP"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
    }
}
