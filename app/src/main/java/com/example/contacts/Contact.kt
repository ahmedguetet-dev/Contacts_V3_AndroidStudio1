package com.example.contacts

import android.graphics.Bitmap

/**
 * Modele immuable : toute modification passe par copy() puis ContactsViewModel.update().
 * Ainsi, "Annuler" ne peut jamais alterer les donnees du contact.
 */
data class Contact(
    val id: Int,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photo: Bitmap? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val displayName: String get() = "$prenom $nom".trim()
}
