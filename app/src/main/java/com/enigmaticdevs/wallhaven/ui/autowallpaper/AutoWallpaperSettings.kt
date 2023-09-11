package com.enigmaticdevs.wallhaven.ui.autowallpaper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.databinding.ActivityAutoWallpaperSettingsBinding
import com.enigmaticdevs.wallhaven.ui.settings.Settings

class AutoWallpaperSettings : AppCompatActivity() {
    private lateinit var binding: ActivityAutoWallpaperSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoWallpaperSettingsBinding.inflate(layoutInflater)
        supportFragmentManager.beginTransaction().replace(R.id.auto_wallpaper_frameLayout,
            AutoWallpaperFragment()
        )
            .commit()
        setContentView(binding.root)
    }

    class AutoWallpaperFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.auto_wallpaper_prefs, rootKey)
        }

    }
}