package com.github.pvtitov.grannys.telephone

import android.telecom.Call
import android.telecom.VideoProfile
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

object CallManager {
    private var call: Call? = null
    private val stateSubject = BehaviorSubject.create<UIState>().also { it.onNext(UIState.READY) }

    fun update(
        call: Call? = this.call,
        state: UIState = this.stateSubject.value ?: UIState.PROCESSING
    ) {

        propagateCallIfNoActiveCall(call, state)
    }

    private fun propagateCallIfNoActiveCall(
        call: Call?,
        state: UIState
    ) {
        if (this.call != null
            && this.call != call
            && this.call!!.state != Call.STATE_DISCONNECTED
        )
            call?.disconnect()
        else {
            automateCall(call, state)
        }
    }

    private fun automateCall(call: Call?, state: UIState) {
        when (state) {
            UIState.RINGING -> {
                propagateCallAndState(call, state)
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        answer()
                    }
                }, Config.autoAnswerDelay)
            }
            else -> propagateCallAndState(call, state)
        }
    }

    private fun propagateCallAndState(
        call: Call?,
        state: UIState
    ) {
        this.call = call
        this.stateSubject.onNext(state)
    }

    fun answer(): String {
        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        return call?.details?.handle?.schemeSpecificPart
            ?: throw IllegalArgumentException("Incoming call number can not be null")
    }

    fun stateEmitter(): Observable<UIState> {
        return stateSubject
    }

    fun getCurrentCall(): Call? {
        return call
    }
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