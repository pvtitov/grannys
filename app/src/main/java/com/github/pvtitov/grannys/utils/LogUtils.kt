package com.github.pvtitov.grannys.utils

import android.util.Log
import com.github.pvtitov.grannys.BuildConfig
import java.util.*

fun eLog(e: Throwable) {
    Log.e("Error_${BuildConfig.APPLICATION_ID
        .substringAfterLast(".")
        .toUpperCase(Locale.US)}", e.message)
}

fun dLog(s: String) {
    Log.d("Debug_${BuildConfig.APPLICATION_ID
        .substringAfterLast(".")
        .toUpperCase(Locale.US)}",
        s)
}