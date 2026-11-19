package com.example.contacts

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

/**
 * Etat temporaire de l'ecran Detail (portee Fragment) : photo choisie mais pas encore enregistree.
 * Survit a la rotation et au passage par la carte ; disparait quand on quitte le Detail.
 */
class DetailEditViewModel : ViewModel() {
    var photoChanged: Boolean = false
    var photo: Bitmap? = null
}
