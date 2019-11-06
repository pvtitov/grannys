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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.github.pvtitov.grannys.telephone.CurrentCallHolder
import com.github.pvtitov.grannys.telephone.Contact
import com.github.pvtitov.grannys.telephone.PhoneBook
import com.github.pvtitov.grannys.telephone.State
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
    private var currentContact = PhoneBook.contacts[0]

    private fun onRinging(number: String) {
        progressLayout.post { progressLayout.visibility = View.GONE }
        phoneIcon.apply {
            setImageResource(R.drawable.ic_phone_ringing)
            phoneIcon.setOnClickListener {
                answer()
            }
        }
    }

    private fun onTalking() {
        progressLayout.post { progressLayout.visibility = View.GONE }
        phoneIcon.apply {
            setImageResource(R.drawable.ic_phone_talking)
            phoneIcon.setOnClickListener {
                reject()
            }
        }
    }

    private fun onIdling() {
        progressLayout.post { progressLayout.visibility = View.GONE }
        phoneIcon.apply {
            setImageResource(R.drawable.ic_phone_idling)
            phoneIcon.setOnClickListener {
                dial(currentContact)
            }
        }
    }

    private fun dial(contact: Contact) {
        if (!contact.hasValidPhoneNumber())
            throw IllegalArgumentException("${contact.phone} is not a valid phone number")
        progressLayout.visibility = View.VISIBLE
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

    private fun answer() {
        progressLayout.visibility = View.VISIBLE
        val incomingCall = currentCallHolder.answer()
        displayCaller(incomingCall)
    }

    private fun reject() {
        progressLayout.visibility = View.VISIBLE
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactsList = view.findViewById<RecyclerView>(R.id.contactsList)
        contactsList.adapter = ContactsAdapter(PhoneBook.contacts)
        contactsList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        PagerSnapHelper().attachToRecyclerView(contactsList)
        contactsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    val i = (contactsList.layoutManager as LinearLayoutManager)
                        .findFirstCompletelyVisibleItemPosition()
                    currentContact = PhoneBook.contacts[i]
                }
            }
        })
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        onIdling()

        currentCallHolder.stateEmitter()
            .subscribe(
                { state ->
                    when (state) {
                        State.IDLING -> onIdling()
                        State.DIALING -> progressLayout.post {
                            progressLayout.visibility = View.VISIBLE
                        }
                        State.PROCESSING -> progressLayout.post {
                            progressLayout.visibility = View.VISIBLE
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

        onPickContact()
    }

    private fun onPickContact() {
        contactsList.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
        }
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
