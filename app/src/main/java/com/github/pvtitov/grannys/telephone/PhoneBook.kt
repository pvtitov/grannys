package com.github.pvtitov.grannys.telephone

import android.content.Context
import android.provider.ContactsContract
import io.reactivex.Completable
import java.util.*

object PhoneBook {
    val contacts: MutableList<Contact> = mutableListOf()

    fun load(context: Context): Completable {

        val cursor = context.contentResolver
            .query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )

        val tmp: MutableList<Contact> = mutableListOf()

        if (cursor == null) return Completable.error(Throwable("Failed to load contacts."))

        while (cursor.moveToNext()) {
            val name = cursor
                .getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            tmp.add(Contact(phone, name))
        }
        cursor.close()

        contacts.clear()
        contacts.addAll(tmp)

        return Completable.complete()
    }
}