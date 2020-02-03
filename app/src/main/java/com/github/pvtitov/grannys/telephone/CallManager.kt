package com.github.pvtitov.grannys.telephone

import android.content.Context
import android.telecom.Call
import io.reactivex.Observable

interface CallManager {
    fun update(context: Context, call: Call?, state: UIState = UIState.PROCESSING)
    fun answer(): String
    fun stateEmitter(): Observable<UIState>
    fun getCurrentCall(): Call?
}

fun Int.toTelephoneState(): UIState {
    return when (this) {
        Call.STATE_RINGING -> UIState.RINGING
        Call.STATE_ACTIVE -> UIState.TALKING
        Call.STATE_DIALING -> UIState.DIALING
        Call.STATE_DISCONNECTED -> UIState.READY
        else -> UIState.PROCESSING
    }
}