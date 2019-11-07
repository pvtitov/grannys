package com.github.pvtitov.grannys.telephone

import androidx.core.text.isDigitsOnly

class GrennysContact(val phone: String, val name: String = "") {

    fun hasValidPhoneNumber(): Boolean {
        return phone.isDigitsOnly()
    }
}