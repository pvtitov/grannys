package com.github.pvtitov.grannys.telephone

object PhoneBook {
    //TODO replace stub
    val contacts = listOf(
        GrennysContact("79991111111", "Петя"),
        GrennysContact("79992222222", "Коля"),
        GrennysContact("79993333333", "Вася"),
        GrennysContact("79994444444", "Инокентий Павлович")
    )

    fun searchByNumber(number: String): GrennysContact? {
        return contacts.find { number == it.phone }
    }
}