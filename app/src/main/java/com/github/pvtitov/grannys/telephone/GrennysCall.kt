package com.github.pvtitov.grannys.telephone

import android.telecom.Call
import android.telecom.VideoProfile
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.lang.IllegalArgumentException

object GrennysCall {
    private var call: Call? = null
    private val stateSubject = BehaviorSubject.create<UIState>().also { it.onNext(UIState.IDLING) }

    fun update(
        call: Call? = this.call,
        state: UIState = this.stateSubject.value ?: UIState.PROCESSING
    ) {
        checkForActiveCall(call, state)
    }

    private fun checkForActiveCall(
        call: Call?,
        state: UIState
    ) {
        if (this.call != null
            && this.call != call
            && this.call!!.state != Call.STATE_DISCONNECTED
        )
            call?.disconnect()
        else {
            this.call = call
            this.stateSubject.onNext(state)
        }
    }

    fun answer(): String {
        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        return call?.details?.handle?.schemeSpecificPart ?:
        throw IllegalArgumentException("Incoming call number can not be null")
    }

    fun reject() {
        call?.disconnect()
    }

    fun stateEmitter(): Observable<UIState> {
        return stateSubject
    }

    fun getCurrentState(): UIState {
        return stateSubject.value ?: UIState.PROCESSING
    }

    fun getCurrentCall(): Call? {
        return call
    }
}