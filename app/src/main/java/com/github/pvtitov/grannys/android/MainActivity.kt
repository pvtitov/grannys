package com.github.pvtitov.grannys.android

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.github.pvtitov.grannys.R
import com.github.pvtitov.grannys.android.cosu.DeviceAdminReceiver
import com.github.pvtitov.grannys.android.telephone.TelephoneFragment
import com.github.pvtitov.grannys.cosu.CosuManager
import com.github.pvtitov.grannys.utils.MultipleClicksTrigger

class MainActivity : AppCompatActivity() {

    private val trigger = MultipleClicksTrigger()
    private lateinit var cosuManager: CosuManager

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
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            trigger.doOnEvent(
                event.x,
                event.y,
                event.downTime
            ) {
                openContactsActivity()
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
