package com.github.pvtitov.grannys

import android.util.Log
import androidx.core.text.isDigitsOnly
import java.util.*

fun Any.eLog(e: Throwable) {
    Log.e(this::class.java.simpleName, e.message)
}

fun Any.dLog(s: String) {
    Log.d(BuildConfig.APPLICATION_ID.substringAfterLast(".").toUpperCase(Locale.US)
            + "\n" + this::class.java.simpleName,
        s)
}