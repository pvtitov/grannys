package com.github.pvtitov.grannys.android.telephone

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.pvtitov.grannys.R
import com.github.pvtitov.grannys.telephone.Contact
import java.util.*

class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val nameTextView: TextView = itemView.findViewById(R.id.contactName)

    fun bind(contact: Contact) {
        nameTextView.text = contact.name
    }
}