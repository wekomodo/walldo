package com.enigmaticdevs.wallhaven.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.databinding.ActivitySettingsBinding
import com.enigmaticdevs.wallhaven.databinding.ItemEditTextBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.DataStoreViewModel
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.util.Status
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        binding.toolbar2.setNavigationOnClickListener {
            finish()
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(mPrefsListener)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment())
            .commit()
        setContentView(binding.root)
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
            }
        }


    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {
        private val dataStoreViewModel: DataStoreViewModel by viewModels()
        private val mainViewModel: MainViewModel by viewModels()
        private var apiKey = ""

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            launchAPIkeyObserver()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_prefs, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            val key: String = preference.key
            //check the key and open dialog
            if (key == "api_key") {
                val binding = ItemEditTextBinding.inflate(LayoutInflater.from(context))
                val view = binding.root
                dataStoreViewModel.getAPIkey()
                dataStoreViewModel.apiKey.observe(viewLifecycleOwner) {
                    binding.apiKeyEditText.setText(it)
                }
                MaterialAlertDialogBuilder(requireContext()).setTitle("Enter API key").setView(view)
                    .setPositiveButton("Save") { _, _ ->
                        apiKey = binding.apiKeyEditText.text.toString()
                        //Send API Request for AUTH
                        mainViewModel.authenticateAPIkey(apiKey)
                    }.setNegativeButton("Reset") { _, _ ->
                        //reset API key in DataStore
                        dataStoreViewModel.saveAPIkey("")
                        //reset API key in sharedPrefs
                        preferenceManager.sharedPreferences?.edit()
                            ?.putString("api_key", "")
                            ?.apply()
                    }.show()
            }
            return super.onPreferenceTreeClick(preference)
        }



        private fun launchAPIkeyObserver() {
            viewLifecycleOwner.lifecycleScope.launch {
                mainViewModel.apiKey.collectLatest { response ->
                    when (response.status) {
                        Status.SUCCESS -> {
                            Toast.makeText(context, "Valid API key", Toast.LENGTH_SHORT).show()
                            //saving API key in DataStore
                            dataStoreViewModel.saveAPIkey(apiKey)
                            //saving API key in sharedPrefs
                            preferenceManager.sharedPreferences?.edit()
                                ?.putString("api_key", apiKey)
                                ?.apply()
                        }
                        Status.ERROR -> {
                            Toast.makeText(context, "Invalid API key", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

