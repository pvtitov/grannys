package com.github.pvtitov.grannys.telephone

import android.content.Context
import android.provider.ContactsContract
import io.reactivex.Completable

object PhoneBook {
    //TODO replace stub
    val contacts: MutableList<GrennysContact> = mutableListOf()

    fun load(context: Context): Completable {

        val cursor = context.contentResolver
            .query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )

        val tmp: MutableList<GrennysContact> = mutableListOf()

        if (cursor == null) return Completable.error(Throwable("Failed to load contacts."))

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            tmp.add(GrennysContact(phone, name))
        }
        cursor.close()

        contacts.clear()
        contacts.addAll(tmp)

        return Completable.complete()
    }
}