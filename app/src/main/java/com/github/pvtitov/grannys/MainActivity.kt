package com.github.pvtitov.grannys

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.github.pvtitov.grannys.telephone.android.TelephoneFragment

class MainActivity : AppCompatActivity() {

    private val trigger = SevenClicksTrigger()
    private lateinit var lockTaskModeManager: LockTaskModeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerLayout, TelephoneFragment.newInstance())
                .commitNow()
        }

        lockTaskModeManager = LockTaskModeManager(
            DeviceAdminReceiver.getComponentName(applicationContext),
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager,
            applicationContext.packageName
        ).also { it.tryTurnOnCosuPolicies() }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            trigger.doOnEvent(
                event.x,
                event.y,
                event.downTime
            ) {
                lockTaskModeManager.stopLockTaskMode(this@MainActivity)
                lockTaskModeManager.tryTurnOffCosuPolicies()
                openContactsActivity()
                finish()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun openContactsActivity() {
        startActivity(
            Intent(Intent.ACTION_VIEW)
                .also { it.type = ContactsContract.Contacts.CONTENT_TYPE }
        )
    }
}
