package com.github.pvtitov.grannys.telephone.android

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.pvtitov.grannys.R
import com.github.pvtitov.grannys.telephone.GrennysContact

class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val nameTextView: TextView = itemView.findViewById(R.id.contactName)

    fun bind(contact: GrennysContact) {
        nameTextView.text = contact.name
    }
}