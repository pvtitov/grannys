package com.github.arekolek.phone

import android.telecom.Call
import android.telecom.InCallService
import com.github.arekolek.phone.telephone.CurrentCallHolder
import com.github.arekolek.phone.telephone.State
import io.reactivex.disposables.CompositeDisposable

class CallService : InCallService() {

    private val currentCallHolder = CurrentCallHolder
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        dLog("SERVICE ==> onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        dLog("SERVICE ==> onDestroy()")
        compositeDisposable.dispose()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        dLog("SERVICE ==> onCallAdded(), call state ${call.state}")
        call.registerCallback(callback)
        currentCallHolder.update(call = call, state = call.state.toTelephoneState())
        callback.onStateChanged(call, call.state)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        dLog("SERVICE ==> onCallRemoved()")
        call.unregisterCallback(callback)
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            dLog("SERVICE ==> State updated: state = ${call.state} ($state)")
            currentCallHolder.update(call = call, state = state.toTelephoneState())
        }
    }

    private fun Int.toTelephoneState(): State {
        return when (this) {
            Call.STATE_RINGING -> State.RINGING
            Call.STATE_ACTIVE -> State.TALKING
            Call.STATE_DIALING -> State.DIALING
            Call.STATE_DISCONNECTED -> State.IDLING
            else -> State.PROCESSING
        }
    }
}