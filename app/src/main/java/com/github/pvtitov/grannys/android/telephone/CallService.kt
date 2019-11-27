package com.github.pvtitov.grannys.android.telephone

import android.telecom.Call
import android.telecom.InCallService
import com.github.pvtitov.grannys.telephone.CallManager
import com.github.pvtitov.grannys.telephone.UIState
import io.reactivex.disposables.CompositeDisposable

class CallService : InCallService() {

    private val callManager = CallManager
    private val compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callback)
        //TODO removed excess update
//        callManager.update(call = call, state = call.state.toTelephoneState())
        callback.onStateChanged(call, call.state)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callback)
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            callManager.update(call = call, state = state.toTelephoneState())
        }
    }

    private fun Int.toTelephoneState(): UIState {
        return when (this) {
            Call.STATE_RINGING -> UIState.RINGING
            Call.STATE_ACTIVE -> UIState.TALKING
            Call.STATE_DIALING -> UIState.DIALING
            Call.STATE_DISCONNECTED -> UIState.IDLING
            else -> UIState.PROCESSING
        }
    }
}