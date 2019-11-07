package com.github.pvtitov.grannys

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.UserManager
import android.provider.Settings
import android.util.Log

private const val HOUR = 60

class LockTaskModeManager(
    private val adminComponentName: ComponentName,
    private val devicePolicyManager: DevicePolicyManager,
    private val packageName: String
) {

    val isDeviceOwner: Boolean
        get() = devicePolicyManager.isDeviceOwnerApp(packageName)

    fun enableActivity(context: Context, activity: Class<MainActivity>) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, activity),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun disableActivity(context: Context, activity: Class<MainActivity>) {
        devicePolicyManager.clearPackagePersistentPreferredActivities(
            adminComponentName,
            packageName
        )
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, activity),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun tryTurnOnCosuPolicies() {
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            setDefaultCosuPolicies(true)
        } else {
            Log.d(
                javaClass.simpleName,
                "App is not a device owner. COSU policies was not turned on."
            )
        }
    }

    fun stopLockTaskMode(activity: Activity) {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_LOCKED) {
            activity.stopLockTask()
        } else {
            Log.d(
                javaClass.simpleName,
                "Activity is not in lock task mode. Should not stop lock task mode."
            )

        }
    }

    @Throws(SecurityException::class)
    fun tryTurnOffCosuPolicies() {
        if (devicePolicyManager.isAdminActive(adminComponentName)) {
            setDefaultCosuPolicies(false)
        } else {
            Log.d(
                javaClass.simpleName,
                "Device admin receiver is absent. COSU policies was not turned off."
            )
        }
    }

    private fun setDefaultCosuPolicies(isActive: Boolean) {

        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, isActive)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, isActive)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, isActive)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, isActive)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, isActive)

        devicePolicyManager.setKeyguardDisabled(adminComponentName, isActive)
        devicePolicyManager.setStatusBarDisabled(adminComponentName, isActive)

        enableStayOnWhilePluggedIn(isActive)

        if (isActive) {
            devicePolicyManager.setSystemUpdatePolicy(
                adminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(0 * HOUR, 2 * HOUR)
            )
        } else {
            devicePolicyManager.setSystemUpdatePolicy(
                adminComponentName,
                null
            )
        }

        devicePolicyManager.setLockTaskPackages(
            adminComponentName,
            if (isActive) arrayOf(packageName) else arrayOf()
        )

        val intentFilter = IntentFilter(Intent.ACTION_MAIN)
        intentFilter.addCategory(Intent.CATEGORY_HOME)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)

        if (isActive) {
            devicePolicyManager.addPersistentPreferredActivity(
                adminComponentName, intentFilter, ComponentName(
                    packageName, SplashActivity::class.java.name
                )
            )
        } else {
            devicePolicyManager.clearPackagePersistentPreferredActivities(
                adminComponentName, packageName
            )
        }
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) {
        if (disallow) {
            devicePolicyManager.addUserRestriction(
                adminComponentName,
                restriction
            )
        } else {
            devicePolicyManager.clearUserRestriction(
                adminComponentName,
                restriction
            )
        }
    }

    private fun enableStayOnWhilePluggedIn(enabled: Boolean) {
        if (enabled) {
            devicePolicyManager.setGlobalSetting(
                adminComponentName,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                Integer.toString(
                    BatteryManager.BATTERY_PLUGGED_AC
                            or BatteryManager.BATTERY_PLUGGED_USB
                            or BatteryManager.BATTERY_PLUGGED_WIRELESS
                )
            )
        } else {
            devicePolicyManager.setGlobalSetting(
                adminComponentName,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                "0"
            )
        }
    }
}
