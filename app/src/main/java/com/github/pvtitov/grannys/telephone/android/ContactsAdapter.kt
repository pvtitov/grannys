package com.github.pvtitov.grannys.telephone.android

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.pvtitov.grannys.R
import com.github.pvtitov.grannys.telephone.GrennysContact

class ContactsAdapter(val contactNames: List<GrennysContact>): RecyclerView.Adapter<ContactViewHolder>() {

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