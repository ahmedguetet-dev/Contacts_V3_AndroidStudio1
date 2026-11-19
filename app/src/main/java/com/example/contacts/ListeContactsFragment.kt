package com.example.contacts

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListeContactsFragment : Fragment(R.layout.fragment_list_contacts) {

    private val vm: ContactsViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var search: EditText
    private lateinit var empty: TextView
    private var allContacts: List<Contact> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.title = getString(R.string.app_name)
        search = view.findViewById(R.id.searchContacts)
        empty = view.findViewById(R.id.emptyMessage)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerContacts)

        adapter = ContactAdapter { contact -> (activity as? MainActivity)?.openDetail(contact.id) }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Filtrage au fil de la saisie, sans bouton de validation.
        search.doAfterTextChanged { applyFilter() }

        // Toute modification dans le ViewModel partage rafraichit la liste immediatement.
        vm.contacts.observe(viewLifecycleOwner) { contacts ->
            allContacts = contacts
            applyFilter()
        }
    }

    private fun applyFilter() {
        val query = search.text.toString()
        val result = ContactFilter.filter(allContacts, query)
        adapter.submitList(result)
        empty.text = if (query.isBlank()) {
            "Aucun contact."
        } else {
            "Aucun contact ne correspond à « ${query.trim()} »."
        }
        empty.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
    }
}
