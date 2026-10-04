package com.example.hardware

import android.content.Context
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FlashlightController(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val cameraId: String? = try {
        cameraManager?.cameraIdList?.firstOrNull { id ->
            val chars = cameraManager.getCameraCharacteristics(id)
            chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    } catch (_: Exception) {
        null
    }

    private var strobeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun setTorch(enabled: Boolean) {
        if (cameraId == null || cameraManager == null) return
        try {
            cameraManager.setTorchMode(cameraId, enabled)
        } catch (_: Exception) {}
    }

    fun startStrobe(flashIntervalMs: Long = 60) {
        if (cameraId == null || cameraManager == null) return
        stopStrobe()
        strobeJob = scope.launch {
            var state = false
            while (isActive) {
                state = !state
                try {
                    cameraManager.setTorchMode(cameraId, state)
                } catch (_: Exception) {}
                delay(flashIntervalMs)
            }
        }
    }

    fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        setTorch(false)
    }
}
