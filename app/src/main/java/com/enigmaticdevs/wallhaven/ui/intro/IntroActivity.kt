package com.enigmaticdevs.wallhaven.ui.intro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.enigmaticdevs.wallhaven.MainActivity
import com.enigmaticdevs.wallhaven.R
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroCustomLayoutFragment
import com.github.appintro.AppIntroPageTransformerType

class IntroActivity : AppIntro() {
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure you don't call setContentView!
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragments
        showStatusBar(true)
        setProgressIndicator()
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_layout_1))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_layout_2))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_layout_3))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_layout_4))
        setTransformer(AppIntroPageTransformerType.Fade)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        preferences.edit().putBoolean("intro_done",true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        preferences.edit().putBoolean("intro_done",true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}