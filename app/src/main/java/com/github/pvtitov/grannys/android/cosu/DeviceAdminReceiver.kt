package com.github.pvtitov.grannys.android.cosu

import android.content.ComponentName
import android.content.Context

class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {
    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, DeviceAdminReceiver::class.java)
        }
    }
}
