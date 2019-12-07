package com.github.pvtitov.grannys.android.telephone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.github.pvtitov.grannys.R
import com.github.pvtitov.grannys.telephone.*
import com.github.pvtitov.grannys.utils.eLog
import com.github.pvtitov.grannys.utils.trimToPhoneNumber
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.progress.*


class TelephoneFragment : Fragment() {

    companion object {
        const val REQUEST_PERMISSION = 1
        fun newInstance() = TelephoneFragment()
    }

    private val compositeDisposable = CompositeDisposable()
    private val callManager = CallManager
    private var currentContact = Contact("", "")

    private lateinit var layoutManager: ScrollableLayoutManager

    private fun onRinging(number: String) {
        setupScreen(isScrollable = false, buttonIcon = R.drawable.ic_phone_ringing)
        displayCaller(number)
    }

    private fun onTalking() {
        setupScreen(isScrollable = false, buttonIcon = R.drawable.ic_phone_talking)
    }

    private fun onIdling() {
        setupScreen(isScrollable = true) {
            dial(currentContact)
        }
    }

    private fun dial(contact: Contact) {
        setupScreen(isLoading = true, isScrollable = false)
        Single.fromCallable { checkPermission() }
            .observeOn(Schedulers.io())
            .subscribe(
                {
                    val uri = Uri.fromParts("tel", contact.phone, null)
                    startActivity(Intent(Intent.ACTION_CALL, uri))
                },
                { eLog(it) }
            ).addTo(compositeDisposable)
    }

    private fun setupScreen(
        isLoading: Boolean = false,
        isScrollable: Boolean,
        isContactVisible: Boolean = true,
        @DrawableRes buttonIcon: Int = R.drawable.ic_phone_idling,
        onClick: () -> Unit = {}
    ) {
        progressLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
        contactsList.visibility = if (isContactVisible) View.VISIBLE else View.INVISIBLE
        layoutManager.isScrollable = isScrollable
        phoneIcon.apply {
            setImageResource(buttonIcon)
            phoneIcon.setOnTouchListener { v, event ->
                val vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    handler.postDelayed(onClick, Config.ringDuration)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(Config.ringDuration, 255))
                    } else {
                        vibrator.vibrate(Config.ringDuration)
                    }
                } else if (event?.action == MotionEvent.ACTION_UP) {
                    handler.removeCallbacksAndMessages(null)
                    vibrator.cancel()
                }
                return@setOnTouchListener true
            }
        }
    }

    private fun displayCaller(number: String) {
        val contact: Contact? = PhoneBook.contacts
            .find {
                val n = it.phone.trimToPhoneNumber()
                number.trimToPhoneNumber() == n
            }
        if (contact != null) {
            val position = PhoneBook.contacts.indexOf(contact)
            contactsList.scrollToPosition(position)
        } else {
            contactsList.visibility = View.INVISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactsList = view.findViewById<RecyclerView>(R.id.contactsList)
        contactsList.adapter =
            ContactsAdapter(PhoneBook.contacts)
        layoutManager = ScrollableLayoutManager(context)
        contactsList.layoutManager = layoutManager
        PagerSnapHelper().attachToRecyclerView(contactsList)
        contactsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    val i = (contactsList.layoutManager as LinearLayoutManager)
                        .findFirstCompletelyVisibleItemPosition()
                    currentContact = PhoneBook.contacts[i].also { it.trim() }
                }
            }
        })
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        onIdling()

        callManager.stateEmitter()
            .subscribe(
                { state ->
                    when (state) {
                        UIState.READY -> onIdling()
                        UIState.DIALING -> progressLayout.visibility = View.VISIBLE
                        UIState.PROCESSING -> progressLayout.visibility = View.VISIBLE
                        UIState.RINGING ->
                            onRinging(
                                callManager.getCurrentCall()?.details?.handle?.schemeSpecificPart
                                    ?: "unknown"
                            )
                        UIState.TALKING -> onTalking()
                    }
                },
                { eLog(it) }
            )
            .addTo(compositeDisposable)
    }

    override fun onStart() {
        super.onStart()
        suggestReplaceDefaultDialer()
        context?.let {
            PhoneBook.load(it)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    contactsList.adapter?.notifyDataSetChanged()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        callManager.getCurrentCall()?.let {
            displayCaller(it.details.handle.schemeSpecificPart)
        }
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onDestroy()
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
