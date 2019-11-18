package com.github.pvtitov.grannys.telephone

class GrennysContact(val phone: String, val name: String) {

    fun hasValidPhoneNumber(): Boolean {
        return phone.matches(Regex("^[+\\d][\\d]*"))
    }

    fun trim(): GrennysContact {
        phone.filterIndexed { i, c -> (i == 0 && (c.isDigit() || c == '+')) || c.isDigit() }
        return GrennysContact(phone = phone, name = name)
    }
}