package com.example.contacts

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Le clic renvoie le Contact lui-meme (donc son id), jamais une position : pas d'erreur d'indice apres filtrage. */
class ContactAdapter(private val onClick: (Contact) -> Unit) :
    RecyclerView.Adapter<ContactAdapter.ContactHolder>() {

    private var items: List<Contact> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Contact>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ContactHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val photo: ImageView = view.findViewById(R.id.contactPhoto)
        private val name: TextView = view.findViewById(R.id.contactName)
        private val phone: TextView = view.findViewById(R.id.contactPhone)

        fun bind(contact: Contact) {
            name.text = contact.displayName
            phone.text = contact.telephone
            ImageUtils.bind(photo, contact.photo)
            itemView.setOnClickListener { onClick(contact) }
        }
    }
}
