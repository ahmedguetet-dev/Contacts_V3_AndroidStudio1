package com.example.contacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

/** Unique activite : heberge les fragments Liste -> Detail -> Carte dans un FragmentContainerView. */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Uniquement au premier lancement : apres rotation, le FragmentManager restaure
        // lui-meme la pile (pas de fragments en double).
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container, ListeContactsFragment())
            }
        }
        supportFragmentManager.addOnBackStackChangedListener { updateUpButton() }
        updateUpButton()
    }

    fun openDetail(contactId: Int) {
        // Evite d'empiler deux Detail en cas de double clic.
        if (currentFragment() !is ListeContactsFragment) return
        navigateTo(DetailContactFragment.newInstance(contactId), "detail")
    }

    fun openMap(contactId: Int, latitude: Double?, longitude: Double?) {
        if (currentFragment() !is DetailContactFragment) return
        navigateTo(CarteContactFragment.newInstance(contactId, latitude, longitude), "map")
    }

    private fun currentFragment(): Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)

    private fun navigateTo(fragment: Fragment, name: String) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container, fragment)
            addToBackStack(name)
        }
        supportFragmentManager.executePendingTransactions()
    }

    private fun updateUpButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
