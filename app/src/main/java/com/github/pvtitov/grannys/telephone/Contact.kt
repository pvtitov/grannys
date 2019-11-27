package com.github.pvtitov.grannys.telephone

class Contact(val phone: String, val name: String) {

    fun hasValidPhoneNumber(): Boolean {
        return phone.matches(Regex("^[+\\d][\\d]*"))
    }

    fun trim(): Contact {
        phone.filterIndexed { i, c -> (i == 0 && (c.isDigit() || c == '+')) || c.isDigit() }
        return Contact(phone = phone, name = name)
    }
}