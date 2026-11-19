package com.example.contacts

import java.text.Normalizer

/** Logique de filtrage pure (sans Android) : facile a tester. */
object ContactFilter {
    private val DIACRITICS = Regex("\\p{M}+")

    /** Minuscules, sans accents, sans espaces en bordure. */
    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .lowercase()
            .trim()

    /** Filtre sur nom, prenom, "prenom nom", "nom prenom" et telephone. Requete vide = tout. */
    fun filter(contacts: List<Contact>, query: String): List<Contact> {
        val q = normalize(query)
        if (q.isEmpty()) return contacts
        val phoneQuery = q.replace(" ", "")
        return contacts.filter { c ->
            normalize(c.nom).contains(q) ||
                normalize(c.prenom).contains(q) ||
                normalize("${c.prenom} ${c.nom}").contains(q) ||
                normalize("${c.nom} ${c.prenom}").contains(q) ||
                normalize(c.telephone).replace(" ", "").contains(phoneQuery)
        }
    }
}
