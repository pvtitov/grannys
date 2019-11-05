package com.github.pvtitov.grannys

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.github.pvtitov.grannys.telephone.CurrentCallHolder
import com.github.pvtitov.grannys.telephone.Person
import com.github.pvtitov.grannys.telephone.PhoneBook
import com.github.pvtitov.grannys.telephone.State
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialer_fragment.*
import kotlinx.android.synthetic.main.progress.*


class DialerFragment : Fragment() {

    companion object {
        const val REQUEST_PERMISSION = 1

        fun newInstance() = DialerFragment()
    }

    private val compositeDisposable = CompositeDisposable()
    private val currentCallHolder = CurrentCallHolder
    private var person = Person("")

    private fun onRinging(number: String) {
        progress_layout.post { progress_layout.visibility = View.GONE }
        phone_icon.apply {
            setImageResource(R.drawable.ic_phone_ringing)
            phone_icon.setOnClickListener {
                answer()
            }
        }
    }

    private fun onTalking() {
        progress_layout.post { progress_layout.visibility = View.GONE }
        phone_icon.apply {
            setImageResource(R.drawable.ic_phone_talking)
            phone_icon.setOnClickListener {
                reject()
            }
        }
    }

    private fun onIdling() {
        progress_layout.post { progress_layout.visibility = View.GONE }
        phone_icon.apply {
            setImageResource(R.drawable.ic_phone_idling)
            phone_icon.setOnClickListener {
                dial(person)
            }
        }
    }

    private fun dial(person: Person) {
        if (!person.hasValidPhoneNumber())
            throw IllegalArgumentException("${person.phone} is not a valid phone number")
        progress_layout.visibility = View.VISIBLE
        Single.fromCallable { checkPermission() }
            .observeOn(Schedulers.io())
            .subscribe(
                {
                    val uri = Uri.fromParts("tel", person.phone, null)
                    startActivity(Intent(Intent.ACTION_CALL, uri))
                },
                { eLog(it) }
            ).addTo(compositeDisposable)
    }

    private fun answer() {
        progress_layout.visibility = View.VISIBLE
        val incomingCall = currentCallHolder.answer()
        displayCaller(incomingCall)
    }

    private fun reject() {
        progress_layout.visibility = View.VISIBLE
        currentCallHolder.reject()
    }

    private fun displayCaller(number: String) {
        //TODO replace stub
        val name = PhoneBook.searchByNumber(number)?.name ?: "No match found"
        Toast.makeText(this.context, name, Toast.LENGTH_SHORT).show()
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
