package com.github.pvtitov.grannys.android

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.KeyEvent
import android.view.WindowManager
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
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

        forceTaskToFront()

        super.onPause()
    }

    private fun forceTaskToFront() {
        val activityManager: ActivityManager = applicationContext
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)
    }

    private fun openContactsActivity() {
        startActivity(
            Intent(Intent.ACTION_VIEW)
                .also { it.type = ContactsContract.Contacts.CONTENT_TYPE }
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = true

    companion object {
        fun start(context: Context) {
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                .let(context::startActivity)
        }
    }
}
