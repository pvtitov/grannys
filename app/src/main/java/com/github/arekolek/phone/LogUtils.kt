package com.github.arekolek.phone

import android.util.Log

fun Any.eLog(e: Throwable) {
    Log.e(this::class.java.simpleName, e.message)
}

fun Any.dLog(s: String) {
//    Log.d(this::class.java.simpleName, s)
    Log.d("MY_DEBUG", s)

}