package com.example.contacts

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

/** Affiche la position d'un contact : carte centree + marqueur portant son nom. */
class CarteContactFragment : Fragment(R.layout.fragment_map_contact), OnMapReadyCallback {

    private val vm: ContactsViewModel by activityViewModels()
    private var position: LatLng? = null
    private var markerTitle: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.title = "Carte"
        val status = view.findViewById<TextView>(R.id.mapStatus)
        val args = requireArguments()

        val contact = vm.getContact(args.getInt(ARG_CONTACT_ID, -1))
        if (contact == null) {
            showMessage(status, "Contact introuvable.")
            return
        }
        markerTitle = contact.displayName
        activity?.title = "Carte – ${contact.displayName}"

        val latitude = if (args.containsKey(ARG_LAT)) args.getDouble(ARG_LAT) else null
        val longitude = if (args.containsKey(ARG_LNG)) args.getDouble(ARG_LNG) else null

        if (latitude == null || longitude == null) {
            showMessage(status, "Position GPS absente pour ${contact.displayName}.\nRevenez au détail et saisissez latitude et longitude.")
            return
        }
        if (latitude.isNaN() || longitude.isNaN() || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            showMessage(status, "Coordonnées invalides (latitude -90 à 90, longitude -180 à 180).")
            return
        }
        if (!hasMapsApiKey()) {
            showMessage(
                status,
                "Clé API Google Maps manquante : la carte ne peut pas s'afficher.\n" +
                    "Ajoutez MAPS_API_KEY dans local.properties (voir README), puis relancez.\n\n" +
                    "Position : " + String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
            )
            return
        }

        position = LatLng(latitude, longitude)
        status.visibility = View.GONE
        // Apres rotation, le childFragmentManager a deja restaure le SupportMapFragment : on le reutilise.
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.mapContainer, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val target = position ?: return
        googleMap.uiSettings.isZoomControlsEnabled = true
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(target)
                .title(markerTitle)
                .snippet(String.format(Locale.US, "%.5f, %.5f", target.latitude, target.longitude))
        )
        marker?.showInfoWindow()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 14f))
    }

    private fun showMessage(status: TextView, message: String) {
        status.text = message
        status.visibility = View.VISIBLE
    }

    /** Vrai si une cle non vide a ete injectee dans le manifeste (evite une carte vide silencieuse). */
    private fun hasMapsApiKey(): Boolean {
        val ctx = requireContext()
        return try {
            @Suppress("DEPRECATION")
            val info = ctx.packageManager.getApplicationInfo(ctx.packageName, PackageManager.GET_META_DATA)
            !info.metaData?.getString("com.google.android.geo.API_KEY").isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val ARG_CONTACT_ID = "contactId"
        private const val ARG_LAT = "latitude"
        private const val ARG_LNG = "longitude"

        fun newInstance(contactId: Int, latitude: Double?, longitude: Double?) = CarteContactFragment().apply {
            arguments = bundleOf(ARG_CONTACT_ID to contactId).apply {
                if (latitude != null) putDouble(ARG_LAT, latitude)
                if (longitude != null) putDouble(ARG_LNG, longitude)
            }
        }
    }
}
