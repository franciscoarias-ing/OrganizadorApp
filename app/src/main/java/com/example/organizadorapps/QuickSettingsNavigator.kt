package com.example.organizadorapps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast

object QuickSettingsNavigator {

    private var isFlashlightOn: Boolean = false
    private var activeTorchCameraId: String? = null

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

    fun openCamera(context: Context) {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        val fallback = context.packageManager.getLaunchIntentForPackage("com.google.android.GoogleCamera")
        open(context, intent, "No se pudo abrir la cámara", fallback)
    }


    fun openSpotify(context: Context) {
        openAppShortcut(context, "com.spotify.music", "Spotify")
    }

    fun openYouTube(context: Context) {
        openAppShortcut(context, "com.google.android.youtube", "YouTube")
    }


    fun openChatGPT(context: Context) {
        openAppShortcut(context, "com.openai.chatgpt", "ChatGPT")
    }

    fun openGemini(context: Context) {
        openAppShortcut(context, "com.google.android.apps.bard", "Gemini")
    }

    fun openNetflix(context: Context) {
        openAppShortcut(context, "com.netflix.mediaclient", "Netflix")
    }

    fun openDisney(context: Context) {
        openAppShortcut(context, "com.disney.disneyplus", "Disney+")
    }

    fun openAppShortcut(context: Context, packageName: String, label: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            open(context, launchIntent, "No se pudo abrir $label")
            return
        }

        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        open(context, marketIntent, "$label no está instalado", browserIntent)
    }
    fun toggleFlashlight(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(context, "Linterna no compatible con esta versión de Android", Toast.LENGTH_SHORT).show()
            return
        }

        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Concede permiso de cámara para usar la linterna", Toast.LENGTH_SHORT).show()
            return
        }

        runCatching {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = activeTorchCameraId ?: findBackCameraWithFlash(cameraManager)
            if (cameraId == null) {
                Toast.makeText(context, "No se encontró una linterna disponible", Toast.LENGTH_SHORT).show()
                return
            }

            val nextState = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, nextState)
            activeTorchCameraId = cameraId
            isFlashlightOn = nextState
        }.onFailure {
            Toast.makeText(context, "No se pudo cambiar la linterna", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findBackCameraWithFlash(cameraManager: CameraManager): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null

        return cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            hasFlash && lensFacing == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    private fun open(
        context: Context,
        intent: Intent,
        errorMessage: String,
        fallback: Intent? = null
    ) {
        runCatching {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure {
            if (fallback != null) {
                runCatching {
                    context.startActivity(fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }.onFailure {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
