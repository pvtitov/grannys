package com.github.pvtitov.grannys.telephone

object PhoneBook {
    //TODO replace stub
    val contacts = listOf(
        Contact("79991111111", "Петя"),
        Contact("79992222222", "Коля"),
        Contact("79993333333", "Вася"),
        Contact("79994444444", "Инокентий Павлович")
    )

    fun searchByNumber(number: String): Contact? {
        return contacts.find { number == it.phone }
    }
}