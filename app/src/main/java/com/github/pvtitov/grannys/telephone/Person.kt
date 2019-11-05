package com.github.pvtitov.grannys.telephone

import androidx.core.text.isDigitsOnly

class Person(val phone: String, val name: String = "") {

    fun hasValidPhoneNumber(): Boolean {
        return name.isDigitsOnly()
    }
}