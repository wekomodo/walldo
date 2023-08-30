package com.enigmaticdevs.wallhaven.ui.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        binding.toolbar2.setNavigationOnClickListener{
            finish()
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(mPrefsListener)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment())
            .commit()
        setContentView(binding.root)
    }

    private suspend fun update(key : String, value : String) {
        val dataStoreKey = stringPreferencesKey(key)

    }

    private val mPrefsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { preference, key ->
                if (key == "theme") {
                    when (preference.getString("theme", "default")) {
                        "light" -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }

                        "dark" -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        }

                        "default" -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        }
                    }
                  /*  val isNightModeOn = preference.getBoolean("theme", false)
                    if (isNightModeOn)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)*/
                }
            }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_prefs, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            val key: String = preference.key
            //check the key and open dialog
            if (key == "api_key") {
                val view = LayoutInflater.from(requireContext()).inflate(
                    R.layout.item_edit_text,
                    null
                )
                val editText = view.findViewById<TextInputEditText>(R.id.api_key_edit_text)
                editText.setText(preference.sharedPreferences?.getString("api_key", ""))
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Enter API key")
                    .setView(view)
                    .setPositiveButton("Save") { _, _ ->
                        val apiKey = editText.text.toString()
                        // if api call is successfull
                        /* preference.sharedPreferences?.edit()
                            ?.putString("api_key", apiKey)
                            ?.apply()*/
                    }
                    .setNegativeButton("Reset") { _, _ ->
                        preference.sharedPreferences?.edit()
                            ?.putString("api_key", "")?.apply()


                    }.show()
            }
            return super.onPreferenceTreeClick(preference)
        }
    }
}

