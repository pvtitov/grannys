package com.github.pvtitov.grannys.telephone

object PhoneBook {
    //TODO replace stub
    val contacts = listOf(
        Person("79991111111", "Петя"),
        Person("79992222222", "Коля"),
        Person("79993333333", "Вася"),
        Person("79994444444", "Маша"),
        Person("79995555555", "Даша"),
        Person("79996666666", "Саша")
    )

    fun searchByNumber(number: String): Person? {
        return contacts.find { number == it.phone }
    }
}