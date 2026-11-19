package com.example.contacts

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels

class DetailContactFragment : Fragment(R.layout.fragment_detail_contact) {

    private val vm: ContactsViewModel by activityViewModels()
    private val edit: DetailEditViewModel by viewModels()

    private var current: Contact? = null
    private lateinit var photoView: ImageView
    private lateinit var nom: EditText
    private lateinit var prenom: EditText
    private lateinit var phone: EditText
    private lateinit var email: EditText
    private lateinit var lat: EditText
    private lateinit var lng: EditText

    private class Coords(val latitude: Double?, val longitude: Double?)

    private val gallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            toast("Aucune image sélectionnée")
        } else {
            val bitmap = ImageUtils.decodeFromUri(requireContext(), uri)
            if (bitmap == null) toast("Impossible de charger cette image") else setPendingPhoto(bitmap)
        }
    }

    private val camera = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap == null) toast("Prise de photo annulée") else setPendingPhoto(bitmap)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.title = "Détail du contact"
        val contact = vm.getContact(requireArguments().getInt(ARG_CONTACT_ID, NO_ID))
        if (contact == null) {
            toast("Contact introuvable")
            parentFragmentManager.popBackStack()
            return
        }
        current = contact

        photoView = view.findViewById(R.id.detailPhoto)
        nom = view.findViewById(R.id.editNom)
        prenom = view.findViewById(R.id.editPrenom)
        phone = view.findViewById(R.id.editTelephone)
        email = view.findViewById(R.id.editEmail)
        lat = view.findViewById(R.id.editLatitude)
        lng = view.findViewById(R.id.editLongitude)

        fillFields(contact)
        showPhoto()

        view.findViewById<Button>(R.id.btnGallery).setOnClickListener {
            startSafely("Aucune application de galerie disponible") { gallery.launch("image/*") }
        }
        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            startSafely("Aucune application caméra disponible") { camera.launch(null) }
        }
        view.findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            toast("Modification annulée")
            parentFragmentManager.popBackStack()
        }
        view.findViewById<Button>(R.id.btnCall).setOnClickListener { call() }
        view.findViewById<Button>(R.id.btnEmail).setOnClickListener { sendEmail() }
        view.findViewById<Button>(R.id.btnMap).setOnClickListener { openMap() }
    }

    private fun fillFields(c: Contact) {
        nom.setText(c.nom)
        prenom.setText(c.prenom)
        phone.setText(c.telephone)
        email.setText(c.email)
        lat.setText(c.latitude?.toString().orEmpty())
        lng.setText(c.longitude?.toString().orEmpty())
    }

    // ---------- Photo ----------

    private fun setPendingPhoto(bitmap: Bitmap) {
        edit.photo = bitmap
        edit.photoChanged = true
        showPhoto()
    }

    private fun showPhoto() {
        val bitmap = if (edit.photoChanged) edit.photo else current?.photo
        ImageUtils.bind(photoView, bitmap)
    }

    // ---------- Validation ----------

    /** Retourne les coordonnees saisies (null/null si les deux champs sont vides) ou null si invalides. */
    private fun readCoordinates(): Coords? {
        lat.error = null
        lng.error = null
        val laText = lat.text.toString().trim().replace(',', '.')
        val loText = lng.text.toString().trim().replace(',', '.')
        if (laText.isEmpty() && loText.isEmpty()) return Coords(null, null)

        val la = laText.toDoubleOrNull()
        val lo = loText.toDoubleOrNull()
        var valid = true
        if (la == null || la.isNaN() || la.isInfinite()) {
            lat.error = if (laText.isEmpty()) "Latitude requise (ou videz aussi la longitude)" else "Latitude non numérique"
            valid = false
        } else if (la < -90.0 || la > 90.0) {
            lat.error = "Latitude : entre -90 et 90"
            valid = false
        }
        if (lo == null || lo.isNaN() || lo.isInfinite()) {
            lng.error = if (loText.isEmpty()) "Longitude requise (ou videz aussi la latitude)" else "Longitude non numérique"
            valid = false
        } else if (lo < -180.0 || lo > 180.0) {
            lng.error = "Longitude : entre -180 et 180"
            valid = false
        }
        return if (valid) Coords(la, lo) else null
    }

    /** Valide tous les champs et construit le contact modifie, ou retourne null (erreurs affichees). */
    private fun buildUpdatedContact(): Contact? {
        val c = current ?: return null
        var valid = true

        val n = nom.text.toString().trim()
        val p = prenom.text.toString().trim()
        val t = phone.text.toString().trim()
        val e = email.text.toString().trim()

        if (n.isEmpty()) { nom.error = "Le nom est obligatoire"; valid = false }
        if (p.isEmpty()) { prenom.error = "Le prénom est obligatoire"; valid = false }
        if (t.isEmpty()) { phone.error = "Le téléphone est obligatoire"; valid = false }
        if (e.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            email.error = "Adresse e-mail invalide"
            valid = false
        }
        val coords = readCoordinates()
        if (coords == null) valid = false
        if (!valid || coords == null) return null

        return c.copy(
            nom = n,
            prenom = p,
            telephone = t,
            email = e,
            photo = if (edit.photoChanged) edit.photo else c.photo,
            latitude = coords.latitude,
            longitude = coords.longitude
        )
    }

    // ---------- Actions ----------

    private fun save() {
        val updated = buildUpdatedContact()
        if (updated == null) {
            toast("Corrigez les champs signalés")
            return
        }
        vm.update(updated)
        toast("Contact enregistré")
        // Retour a la liste, qui se rafraichit seule via le ViewModel partage.
        parentFragmentManager.popBackStack()
    }

    private fun openMap() {
        val c = current ?: return
        val coords = readCoordinates()
        if (coords == null) {
            toast("Coordonnées invalides : corrigez-les pour voir la carte")
            return
        }
        // La carte affiche ce qui est saisi, sans enregistrer le reste de la fiche.
        (activity as? MainActivity)?.openMap(c.id, coords.latitude, coords.longitude)
    }

    private fun call() {
        val number = phone.text.toString().trim()
        if (number.isEmpty()) {
            phone.error = "Numéro obligatoire pour appeler"
            return
        }
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))
        startSafely("Aucune application d'appel disponible") { startActivity(intent) }
    }

    private fun sendEmail() {
        val address = email.text.toString().trim()
        if (address.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
            email.error = "Adresse e-mail valide requise"
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null))
        startSafely("Aucune application e-mail disponible") { startActivity(intent) }
    }

    /** Lance une application externe sans planter si aucune application ne peut repondre. */
    private fun startSafely(errorMessage: String, action: () -> Unit) {
        try {
            action()
        } catch (e: ActivityNotFoundException) {
            toast(errorMessage)
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_CONTACT_ID = "contactId"
        private const val NO_ID = -1

        fun newInstance(contactId: Int) = DetailContactFragment().apply {
            arguments = bundleOf(ARG_CONTACT_ID to contactId)
        }
    }
}
