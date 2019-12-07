package com.github.pvtitov.grannys.utils

import android.util.Log
import com.github.pvtitov.grannys.BuildConfig
import java.util.*

fun Any.eLog(e: Throwable) {
    Log.e("Error ${BuildConfig.APPLICATION_ID
        .substringAfterLast(".")
        .toUpperCase(Locale.US)}", e.message)
}

fun Any.dLog(s: String) {
    Log.d("Debug ${BuildConfig.APPLICATION_ID
        .substringAfterLast(".")
        .toUpperCase(Locale.US)}",
        s)
}