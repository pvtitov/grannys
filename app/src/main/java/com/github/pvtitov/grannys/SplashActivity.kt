package com.github.pvtitov.grannys

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class SplashActivity : AppCompatActivity() {

    private var lockTaskModeManager: LockTaskModeManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        lockTaskModeManager = LockTaskModeManager(
            DeviceAdminReceiver.getComponentName(this),
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager,
            getApplicationContext().getPackageName()
        )

        if (lockTaskModeManager!!.isDeviceOwner) {
            lockTaskModeManager!!.enableActivity(getApplicationContext(), MainActivity::class.java)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        } else {
            //TODO maybe alert
            Toast.makeText(this, getString(R.string.app_not_whitelisted), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}