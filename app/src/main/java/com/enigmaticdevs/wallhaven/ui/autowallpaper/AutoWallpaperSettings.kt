package com.enigmaticdevs.wallhaven.ui.autowallpaper

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.util.LinkifyCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.databinding.ActivityAutoWallpaperSettingsBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.DataStoreViewModel
import com.enigmaticdevs.wallhaven.services.AutoWallpaperWork
import com.enigmaticdevs.wallhaven.util.customToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class AutoWallpaperSettings : AppCompatActivity() {
    private lateinit var binding: ActivityAutoWallpaperSettingsBinding
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private var params: Params = Params("100", "111", "", "")
    private lateinit var preferences: SharedPreferences
    private val AUTO_WALLPAPER_WORK_ID = "AUTO_WALLPAPER"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoWallpaperSettingsBinding.inflate(layoutInflater)
        supportFragmentManager.beginTransaction().replace(
            R.id.auto_wallpaper_frameLayout,
            AutoWallpaperFragment()
        )
            .commit()
        dataStoreViewModel.readSettings()
        dataStoreViewModel.settings.observe(this) {
            if (it.purity != "null") {
                params = it
            }
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(mPrefsListener)


        initOnClickListeners()
        setContentView(binding.root)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val mPrefsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { preference, key ->
            if (key == "auto_wallpaper") {
                setWorkManager(preference, key)
            }
            }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setWorkManager(preference: SharedPreferences, key: String) {
        //check the key and open dialog
        if (key == "auto_wallpaper" || key == "only_on_wifi" || key == "only_on_charging" || key == "only_when_charging" || key == "auto_wall_interval" || key == "wallpaper_source") {
            /*   billingViewModel.walldoProLiveData.observe(this) {
                   if (it?.entitled == true) {*/

            val autoWallpaper = preference.getBoolean("auto_wallpaper", false)
            if (autoWallpaper) {
                val wifiMust = preference.getBoolean("only_on_wifi", false)
                val chargingMust = preference.getBoolean("only_on_charging", false)
                val idleMust = preference.getBoolean("only_when_idle", false)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(if (wifiMust) NetworkType.UNMETERED else NetworkType.NOT_REQUIRED)
                    .setRequiresCharging(chargingMust)
                    .setRequiresDeviceIdle(idleMust)
                val interval = preference.getString("auto_wall_interval", "1440")?.toLong() ?: 1440
                val data: Data.Builder = Data.Builder()
                data.putString("source", preference.getString("wallpaper_source", "random"))
                data.putString("purity", params.purity)
                data.putString("category", params.category)
                data.putString("ratio", params.ratio)
                data.putString("resolution", params.resolution)
                data.putString("screen", preference.getString("auto_wall_screen", "both"))
                val workRequest =
                    PeriodicWorkRequest.Builder(
                        AutoWallpaperWork::class.java,
                        interval * 60 * 1000,
                        TimeUnit.MILLISECONDS
                    ).setInputData(data.build()).setConstraints(constraints.build()).build()
                WorkManager.getInstance(this).cancelUniqueWork(AUTO_WALLPAPER_WORK_ID)
                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    AUTO_WALLPAPER_WORK_ID,
                    ExistingPeriodicWorkPolicy.UPDATE,  //Existing Periodic Work policy
                    workRequest //work request
                )
                // one time workRequest to test AutoWallpaper
                  /* val workRequest =
                    OneTimeWorkRequestBuilder<AutoWallpaperWork>()
                        .setInitialDelay(Duration.ofSeconds(5))
                        .setInputData(data.build())
                        .setConstraints(constraints.build())
                        .build()
                WorkManager.getInstance(this).enqueue(workRequest)*/
                customToast(this, "Worker is set")
            } else {
                customToast(this, "Worker is cancelled")
                WorkManager.getInstance(this)
                    .cancelUniqueWork(AUTO_WALLPAPER_WORK_ID)
            }
        } /*else {
                        findPreference<SwitchPreference>("auto_wallpaper")?.isChecked = false
                        context?.startActivity(Intent(context, Upgrade::class.java))
                    }*/
    }

    private fun initOnClickListeners() {
        binding.autoWallpaperInfo.setOnClickListener {
            val textView = TextView(this).apply {
                val spannableString = SpannableString(
                    "Try disabling the idle condition and battery \n" +
                            "optimization for this app \n" +
                            "\n" +
                            "For specific instructions:\n" +
                            "https://dontkillmyapp.com"
                )
                LinkifyCompat.addLinks(spannableString, Linkify.WEB_URLS)
                text = spannableString
                movementMethod = LinkMovementMethod.getInstance()
                setTextAppearance(android.R.style.TextAppearance_Small)
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.keyline_8),
                    resources.getDimensionPixelSize(R.dimen.keyline_5),
                    resources.getDimensionPixelSize(R.dimen.keyline_8),
                    resources.getDimensionPixelSize(R.dimen.keyline_0)
                )
            }

            MaterialAlertDialogBuilder(this@AutoWallpaperSettings)
                .setTitle("Not Working?")
                .setView(textView)
                .setPositiveButton("Ok", null)
                .show()
        }
        binding.materialToolbar2.setNavigationOnClickListener {
            finish()
        }
    }

    class AutoWallpaperFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.auto_wallpaper_prefs, rootKey)
        }
    }
}

