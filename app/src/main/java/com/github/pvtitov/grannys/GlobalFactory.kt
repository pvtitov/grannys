package com.github.pvtitov.grannys

import com.github.pvtitov.grannys.telephone.CallManager
import com.github.pvtitov.grannys.telephone.CallManagerImpl

object GlobalFactory {
    val callManager: CallManager = CallManagerImpl
}