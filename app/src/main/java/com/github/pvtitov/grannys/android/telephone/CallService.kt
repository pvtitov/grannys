package com.github.pvtitov.grannys.android.telephone

import android.telecom.Call
import android.telecom.InCallService
import com.github.pvtitov.grannys.GlobalFactory
import com.github.pvtitov.grannys.telephone.CallManager
import com.github.pvtitov.grannys.telephone.toTelephoneState
import io.reactivex.disposables.CompositeDisposable

class CallService : InCallService() {

    private val callManager: CallManager = GlobalFactory.callManager
    private val compositeDisposable = CompositeDisposable()

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            callManager.update(applicationContext, call, state.toTelephoneState())
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callback)
        callback.onStateChanged(call, call.state)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callback)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}