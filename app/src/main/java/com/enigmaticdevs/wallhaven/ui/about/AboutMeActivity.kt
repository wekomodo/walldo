package com.enigmaticdevs.wallhaven.ui.about

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.databinding.ActivityAboutMeBinding

class AboutMeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAboutMeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutMeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(R.id.aboutMe_frameLayout,
            AboutFragment()
        ).commit()
        binding.materialToolbar3.setNavigationOnClickListener{
            finish()
        }
    }
    class AboutFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.about_prefs, rootKey)
        }
    }
}