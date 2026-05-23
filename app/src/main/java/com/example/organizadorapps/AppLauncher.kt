package com.example.organizadorapps

import android.content.Context
import android.widget.Toast

object AppLauncher {

    fun openApp(context: Context, packageName: String, appName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "No se pudo abrir $appName", Toast.LENGTH_SHORT).show()
        }
    }
}