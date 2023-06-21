package net.donething.pc_phone.ui.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import net.donething.pc_phone.R

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val customDataStore = CustomDataStore(requireActivity().dataStore)
        preferenceManager.preferenceDataStore = customDataStore

        setPreferencesFromResource(R.xml.fragment_preferences, rootKey)
    }
}