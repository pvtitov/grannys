package com.github.pvtitov.grannys.android

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import com.github.pvtitov.grannys.R
import com.github.pvtitov.grannys.android.cosu.DeviceAdminReceiver
import com.github.pvtitov.grannys.android.telephone.TelephoneFragment
import com.github.pvtitov.grannys.cosu.CosuManager
import com.github.pvtitov.grannys.utils.ShakeDetector

class MainActivity : AppCompatActivity() {

    private lateinit var cosuManager: CosuManager
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerLayout, TelephoneFragment.newInstance())
                .commitNow()
        }

        cosuManager = CosuManager(
            DeviceAdminReceiver.getComponentName(
                applicationContext
            ),
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager,
            applicationContext.packageName
        )
        cosuManager.tryTurnOnCosuPolicies()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector()
        shakeDetector.setOnShakeListener(object: ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                if (count > 3) {
                    openContactsActivity()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        sensorManager.unregisterListener(shakeDetector)
        super.onPause()
    }

    private fun openContactsActivity() {
        startActivity(
            Intent(Intent.ACTION_VIEW)
                .also { it.type = ContactsContract.Contacts.CONTENT_TYPE }
        )
    }
}
