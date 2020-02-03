package com.github.pvtitov.grannys.telephone

import android.content.Context
import android.telecom.Call
import android.telecom.VideoProfile
import com.github.pvtitov.grannys.android.MainActivity
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

object CallManagerImpl: CallManager {
    private var call: Call? = null
    private val stateSubject = BehaviorSubject.create<UIState>().also { it.onNext(UIState.READY) }

    override fun update(
        context: Context,
        call: Call?,
        state: UIState
    ) {

        propagateCallIfNoActiveCall(context, call, state)
    }

    private fun propagateCallIfNoActiveCall(
        context: Context,
        call: Call?,
        state: UIState
    ) {
        if (this.call != null
            && this.call != call
            && this.call!!.state != Call.STATE_DISCONNECTED
        )
            call?.disconnect()
        else {
            automateCall(context, call, state)
        }
    }

    private fun automateCall(context: Context, call: Call?, state: UIState) {
        when (state) {
            UIState.RINGING -> {
                MainActivity.start(context)
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

    override fun answer(): String {
        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        return call?.details?.handle?.schemeSpecificPart
            ?: throw IllegalArgumentException("Incoming call number can not be null")
    }

    override fun stateEmitter(): Observable<UIState> {
        return stateSubject
    }

    override fun getCurrentCall(): Call? {
        return call
    }
}