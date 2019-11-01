package com.github.arekolek.phone.telephone

import android.telecom.Call
import android.telecom.VideoProfile
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

object CurrentCallHolder {
    private var call: Call? = null
    private val stateSubject = BehaviorSubject.create<State>().also { it.onNext(State.IDLING) }

    fun update(
        call: Call? = this.call,
        state: State = this.stateSubject.value ?: State.PROCESSING
    ) {
        checkForActiveCall(call, state)
    }

    private fun checkForActiveCall(
        call: Call?,
        state: State
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

    fun answer() {
        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun reject() {
        call?.disconnect()
    }

    fun stateEmitter(): Observable<State> {
        return stateSubject
    }

    fun getCurrentState(): State {
        return stateSubject.value ?: State.PROCESSING
    }

    fun getCurrentCall(): Call? {
        return call
    }
}