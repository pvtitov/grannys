package com.github.pvtitov.grannys

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.pvtitov.grannys.telephone.Contact

class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val nameTextView: TextView = itemView.findViewById(R.id.contactName)

    fun bind(contact: Contact) {
        nameTextView.text = contact.name
    }
}