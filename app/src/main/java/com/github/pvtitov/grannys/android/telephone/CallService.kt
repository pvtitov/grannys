package com.github.pvtitov.grannys.android.telephone

import android.telecom.Call
import android.telecom.InCallService
import com.github.pvtitov.grannys.telephone.CallManager
import com.github.pvtitov.grannys.telephone.toTelephoneState
import com.github.pvtitov.grannys.utils.dLog
import io.reactivex.disposables.CompositeDisposable

class CallService : InCallService() {

    private val callManager = CallManager
    private val compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        dLog("SERVICE  onDestroy()")

        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun onCallAdded(call: Call) {
        dLog("SERVICE  onCallAdded()")

        super.onCallAdded(call)
        call.registerCallback(callback)
        //TODO removed excess update
//        callManager.update(call = call, state = call.state.toTelephoneState())
        callback.onStateChanged(call, call.state)
    }

    override fun onCallRemoved(call: Call) {
        dLog("SERVICE  onCallRemoved()")

        super.onCallRemoved(call)
        call.unregisterCallback(callback)
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            dLog("SERVICE  onStateChanged() ${state.stateName()}")

            callManager.update(call = call, state = state.toTelephoneState())
        }
    }

    private fun Int.stateName(): String =
        when (this) {
            Call.STATE_NEW -> "STATE_NEW"
            Call.STATE_DIALING -> "STATE_DIALING"
            Call.STATE_RINGING -> "STATE_RINGING"
            Call.STATE_HOLDING -> "STATE_HOLDING"
            Call.STATE_ACTIVE -> "STATE_ACTIVE"
            Call.STATE_DISCONNECTED -> "STATE_DISCONNECTED"
            Call.STATE_SELECT_PHONE_ACCOUNT -> "STATE_SELECT_PHONE_ACCOUNT"
            Call.STATE_CONNECTING -> "STATE_CONNECTING"
            Call.STATE_DISCONNECTING -> "STATE_DISCONNECTING"
            Call.STATE_PULLING_CALL -> "STATE_PULLING_CALL"
            else -> "CAN NOT DEFINE STATE"
        }
}