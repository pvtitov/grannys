package com.github.pvtitov.grannys.android.telephone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.pvtitov.grannys.R
import com.github.pvtitov.grannys.telephone.Contact

class ContactsAdapter(val contactNames: List<Contact>): RecyclerView.Adapter<ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_holder, parent, false)
        return ContactViewHolder(root)
    }

    override fun getItemCount(): Int {
        return contactNames.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contactNames[position])
    }
}