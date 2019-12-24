package com.github.pvtitov.grannys;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public enum FlashlightManager {

    INSTANCE;

    public boolean flashLightOn(Activity activity) {
        if (hasNoPermission(activity)) {
            Log.i(getClass().getSimpleName(), "No permission granted for flashlight");
            return false;
        }
        if (hasNoSystemFeature(activity)) {
            Log.i(getClass().getSimpleName(), "No such feature as flashlight");
            return false;
        }
        CameraManager cameraManager = (CameraManager)
                activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            return true;
        } catch (CameraAccessException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    public boolean flashLightOff(Activity activity) {
        if (hasNoPermission(activity)) {
            Log.i(getClass().getSimpleName(), "No permission granted for flashlight");
            return false;
        }
        if (hasNoSystemFeature(activity)) {
            Log.i(getClass().getSimpleName(), "No such feature as flashlight");
            return false;
        }
        CameraManager cameraManager = (CameraManager)
                activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            return true;
        } catch (CameraAccessException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    private boolean hasNoSystemFeature(Activity activity) {
        return !activity.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean hasNoPermission(Activity activity) {
        return !(ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED);
    }
}
