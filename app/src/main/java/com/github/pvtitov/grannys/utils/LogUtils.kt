package com.github.pvtitov.grannys.utils

import android.util.Log
import com.github.pvtitov.grannys.BuildConfig
import java.util.*

fun Any.eLog(e: Throwable) {
    Log.e(this::class.java.simpleName, e.message)
}

fun Any.dLog(s: String) {
    Log.d(
        BuildConfig.APPLICATION_ID.substringAfterLast(".").toUpperCase(Locale.US)
            + "\n" + this::class.java.simpleName,
        s)
}