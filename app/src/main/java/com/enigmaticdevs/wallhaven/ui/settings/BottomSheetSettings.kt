package com.enigmaticdevs.wallhaven.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.databinding.BottomsheetSettingsBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.DataStoreViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetSettings : BottomSheetDialogFragment() {
    private lateinit var binding: BottomsheetSettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottomsheet_settings, container, false)
        binding = BottomsheetSettingsBinding.bind(view)
        childFragmentManager.beginTransaction().replace(
           R.id.bss_frameLayout,
            LocalFragment()
        ).commit()
        return binding.root
    }

    @AndroidEntryPoint
    class LocalFragment : PreferenceFragmentCompat() {

        private lateinit var preferences: SharedPreferences
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            context?.let {
                preferences = PreferenceManager.getDefaultSharedPreferences(it)
                preferences.registerOnSharedPreferenceChangeListener(mPrefsListener)
            }
            setPreferencesFromResource(R.xml.bottom_sheet_settings_prefs, rootKey)
        }

        private fun savePrefsToDatastore() {
            val dataStoreViewModel: DataStoreViewModel by viewModels()
            val stringSet = preferences.getStringSet("filter_ratio", HashSet())
            var ratio = ""
            if (stringSet != null) {
                for (items in stringSet) {
                    ratio = "$ratio$items,"
                }
            }
            Log.d("ratiosString", ratio)
            val resolution = preferences.getString("filter_resolution", "").toString()
            val general = preferences.getBoolean("general_category", true)
            val anime = preferences.getBoolean("anime_category", true)
            val people = preferences.getBoolean("people_category", false)
            val category = general.viaString() + anime.viaString() + people.viaString()
            val sfw = preferences.getBoolean("purity_sfw", true)
            val sketchy = preferences.getBoolean("purity_sketchy", false)
            val nsfw = preferences.getBoolean("purity_nsfw", false)
            val purity = sfw.viaString() + sketchy.viaString() + nsfw.viaString()

            dataStoreViewModel.saveSettings(params = Params(purity,category,ratio,resolution))
        }
        private val mPrefsListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "purity_sfw" || key == "purity_sketchy" || key == "purity_nsfw" || key == "general_category" || key == "anime_category" || key == "people_category"|| key == "filter_ratio" || key == "filter_resolution") {
                    //read from sharedPrefs & save to Datastore
                    savePrefsToDatastore()
                }

            }

        private fun Boolean.viaString() = if (this) "1" else "0"
    }
}