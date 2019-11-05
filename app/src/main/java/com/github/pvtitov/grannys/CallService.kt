package com.github.pvtitov.grannys

import android.telecom.Call
import android.telecom.InCallService
import com.github.pvtitov.grannys.telephone.CurrentCallHolder
import com.github.pvtitov.grannys.telephone.State
import io.reactivex.disposables.CompositeDisposable

class CallService : InCallService() {

    private val currentCallHolder = CurrentCallHolder
    private val compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callback)
        currentCallHolder.update(call = call, state = call.state.toTelephoneState())
        callback.onStateChanged(call, call.state)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callback)
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
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