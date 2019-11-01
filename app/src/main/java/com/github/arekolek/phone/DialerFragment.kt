package com.github.arekolek.phone

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.github.arekolek.phone.telephone.CurrentCallHolder
import com.github.arekolek.phone.telephone.State
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialer_fragment.*


class DialerFragment : Fragment() {

    companion object {
        const val REQUEST_PERMISSION = 1

        fun newInstance() = DialerFragment()
    }

    private val compositeDisposable = CompositeDisposable()
    private val currentCallHolder = CurrentCallHolder

    private fun onRinging(number: String) {
        dLog("FRAGMENT ==> RINGING...")
        progress_layout.post { progress_layout.visibility = View.GONE }
        phone_icon.apply {
            setImageResource(R.drawable.ic_phone_ringing)
            phone_icon.setOnClickListener {
                dLog("FRAGMENT ==> ANSWERED!")
                answer()
            }
        }
    }

    private fun onTalking() {
        dLog("FRAGMENT ==> TALKING...")
        progress_layout.post { progress_layout.visibility = View.GONE }
        phone_icon.apply {
            setImageResource(R.drawable.ic_phone_talking)
            phone_icon.setOnClickListener {
                dLog("FRAGMENT ==> HANGUP!")
                reject()
            }
        }
    }

    private fun onIdling() {
        dLog("FRAGMENT ==> IDLING...")
        progress_layout.post { progress_layout.visibility = View.GONE }
        phone_icon.apply {
            setImageResource(R.drawable.ic_phone_idling)
            phone_icon.setOnClickListener {
                dLog("FRAGMENT ==> DIAL!")
                dial("89991234567")
            }
        }
    }

    private fun dial(number: String) {
        progress_layout.visibility = View.VISIBLE
        if (!number.isDigitsOnly()) throw IllegalArgumentException("$number is not a valid phone number")
        Single.fromCallable { checkPermission() }
            .observeOn(Schedulers.io())
            .subscribe(
                {
                    dLog("FRAGMENT ==> permission granted")
                    val uri = Uri.fromParts("tel", number, null)
                    dLog("FRAGMENT ==> Start activity by URI $uri")
                    startActivity(Intent(Intent.ACTION_CALL, uri))
                },
                { eLog(it) }
            ).addTo(compositeDisposable)
    }

    private fun answer() {
        progress_layout.visibility = View.VISIBLE
        currentCallHolder.answer()
    }

    private fun reject() {
        progress_layout.visibility = View.VISIBLE
        currentCallHolder.reject()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        onIdling()

        currentCallHolder.stateEmitter()
            .subscribe(
                { state ->
                    dLog("FRAGMENT ==> State updated with $state")
                    when (state) {
                        State.IDLING -> onIdling()
                        State.DIALING -> progress_layout.post {
                            progress_layout.visibility = View.VISIBLE
                        }
                        State.PROCESSING -> progress_layout.post {
                            progress_layout.visibility = View.VISIBLE
                        }
                        State.RINGING ->
                            onRinging(
                                currentCallHolder.getCurrentCall()?.details?.handle?.schemeSpecificPart
                                    ?: "unknown"
                            )
                        State.TALKING -> onTalking()
                    }
                },
                { eLog(it) }
            )
            .addTo(compositeDisposable)
    }

    override fun onStart() {
        super.onStart()
        suggestReplaceDefaultDialer()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
            }
        }
    }

    private fun checkPermission(): Boolean {
        val activity = activity ?: return false
        if (PermissionChecker.checkSelfPermission(
                activity,
                Manifest.permission.CALL_PHONE
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            return true
        } else {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_PERMISSION
            )
            return false
        }
    }


    private fun suggestReplaceDefaultDialer() {
        val activity = activity ?: return
        if (activity.getSystemService(TelecomManager::class.java)?.defaultDialerPackage != activity.packageName) {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    activity.packageName
                )
                .let(::startActivity)
        }
    }
}
