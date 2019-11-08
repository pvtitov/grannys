package com.github.pvtitov.grannys.android

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.github.pvtitov.grannys.R
import com.github.pvtitov.grannys.android.cosu.DeviceAdminReceiver
import com.github.pvtitov.grannys.cosu.CosuManager

class SplashActivity : AppCompatActivity() {

    private var cosuManager: CosuManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        cosuManager = CosuManager(
            DeviceAdminReceiver.getComponentName(this),
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager,
            getApplicationContext().getPackageName()
        )
        if (cosuManager!!.isDeviceOwner) {
            cosuManager!!.enableActivity(getApplicationContext(), MainActivity::class.java)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        } else {
            //TODO maybe alert
            Toast.makeText(this, getString(R.string.app_not_whitelisted), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}