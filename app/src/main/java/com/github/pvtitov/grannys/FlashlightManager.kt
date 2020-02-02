package com.github.pvtitov.grannys

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.util.Log

import androidx.core.content.ContextCompat

enum class FlashlightManager {

    INSTANCE;

    fun flashLightOn(activity: Activity): Boolean {
        if (hasNoPermission(activity)) {
            Log.i(javaClass.simpleName, "No permission granted for flashlight")
            return false
        }
        if (hasNoSystemFeature(activity)) {
            Log.i(javaClass.simpleName, "No such feature as flashlight")
            return false
        }
        val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
            return true
        } catch (e: CameraAccessException) {
            Log.e(javaClass.simpleName, e.message)
            return false
        }

    }

    fun flashLightOff(activity: Activity): Boolean {
        if (hasNoPermission(activity)) {
            Log.i(javaClass.simpleName, "No permission granted for flashlight")
            return false
        }
        if (hasNoSystemFeature(activity)) {
            Log.i(javaClass.simpleName, "No such feature as flashlight")
            return false
        }
        val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
            return true
        } catch (e: CameraAccessException) {
            Log.e(javaClass.simpleName, e.message)
            return false
        }

    }

    private fun hasNoSystemFeature(activity: Activity): Boolean {
        return !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun hasNoPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }
}
