package com.github.pvtitov.grannys.utils

fun String.trimToPhoneNumber(): String {
    return this.filterIndexed { i, c -> (i == 0 && (c.isDigit() || c == '+')) || c.isDigit() }
}