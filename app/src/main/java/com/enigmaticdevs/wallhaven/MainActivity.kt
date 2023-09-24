package com.enigmaticdevs.wallhaven


import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.databinding.ActivityMainBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.DataStoreViewModel
import com.enigmaticdevs.wallhaven.ui.about.AboutMeActivity
import com.enigmaticdevs.wallhaven.ui.adapters.ViewPagerFragmentAdapter
import com.enigmaticdevs.wallhaven.ui.autowallpaper.AutoWallpaperSettings
import com.enigmaticdevs.wallhaven.ui.fragments.PopularFragment
import com.enigmaticdevs.wallhaven.ui.fragments.RecentFragment
import com.enigmaticdevs.wallhaven.ui.search.SearchActivity
import com.enigmaticdevs.wallhaven.ui.settings.BottomSheetSettings
import com.enigmaticdevs.wallhaven.ui.settings.Settings
import com.enigmaticdevs.wallhaven.ui.upgrade.UpgradeActivity
import com.enigmaticdevs.wallhaven.util.customToast
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavDrawer()
        binding.fragmentContainer.offscreenPageLimit = 2
        //run one time migration from SharedPrefs to Datastore
        savePrefsToDatastore()
        //initialize FragmentContainer
        initFragmentAdapter()
        binding.searchView.setOnClickListener{
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.fabSettings.setOnClickListener{
            val bottomSheetSettings = BottomSheetSettings()
            bottomSheetSettings.show(supportFragmentManager, "BottomSheetDialog")
        }
    }

    private fun initNavDrawer() {
        setSupportActionBar(binding.toolbar)
        //remove title from toolbar
        supportActionBar?.title = ""
        // added a toggle to the toolbar to
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        toggle.syncState()
        // navigation for drawer items
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_autowallpaper -> startActivity(Intent(this, AutoWallpaperSettings::class.java))
                R.id.nav_settings -> startActivity(Intent(this, Settings::class.java))
                R.id.nav_aboutMe -> startActivity(Intent(this, AboutMeActivity::class.java))
                R.id.nav_upgrade -> startActivity(Intent(this, UpgradeActivity::class.java))
                else -> {
                    customToast(this,"message")
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }
        // onBackPressed override to check if navDrawer is open or not. If open then close it first.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }

    private fun initFragmentAdapter() {
        val tabNames = arrayOf("Popular", "Recent")
        val adapter = ViewPagerFragmentAdapter(this).apply {
            addFragment(PopularFragment(), tabNames[0])
            addFragment(RecentFragment(), tabNames[1])
        }
        binding.apply {
           // tabLayout.tabRippleColor = null
            fragmentContainer.adapter = adapter
            TabLayoutMediator(tabLayout, fragmentContainer) { tab, position ->
                tab.text = tabNames[position]
                binding.fragmentContainer.setCurrentItem(tab.position, true)
            }.attach()
        }
        // backup layout using bottom nav bar
        /*      binding.popularButton.setOnClickListener {
                  binding.fragmentContainer.currentItem = 0
              }
              binding.recentsButton.setOnClickListener {
                  binding.fragmentContainer.currentItem = 1
              }
              binding.fragmentContainer.registerOnPageChangeCallback(object :
                  ViewPager2.OnPageChangeCallback() {

                  override fun onPageScrolled(
                      position: Int,
                      positionOffset: Float,
                      positionOffsetPixels: Int
                  ) {
                  }

                  override fun onPageSelected(position: Int) {
                      if (position == 0) {
                          binding.popularButtonText.visibility = View.VISIBLE
                          binding.recentButtonText.visibility = View.GONE
                      } else {
                          binding.recentButtonText.visibility = View.VISIBLE
                          binding.popularButtonText.visibility = View.GONE
                      }
                  }
              })*/
    }
    private fun savePrefsToDatastore() {
        dataStoreViewModel.getSettingsMigrated()
        dataStoreViewModel.settingsMigrated.observe(this){
            if(it== null){
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                val stringSet = preferences.getStringSet("filter_ratio", HashSet())
                var ratio = ""
                if (stringSet != null) {
                    for (items in stringSet) {
                        ratio = "$ratio$items,"
                    }
                }
                val resolution = preferences.getString("filter_resolution", "").toString()
                val general = preferences.getBoolean("general_category", true)
                val anime = preferences.getBoolean("anime_category", true)
                val people = preferences.getBoolean("people_category", false)
                val category = general.viaString() + anime.viaString() + people.viaString()
                val sfw = preferences.getBoolean("purity_sfw", true)
                val sketchy = preferences.getBoolean("purity_sketchy", false)
                val nsfw = preferences.getBoolean("purity_nsfw", false)
                val purity = sfw.viaString() + sketchy.viaString() + nsfw.viaString()
                val apiKey = preferences.getString("api_key","").toString()
                dataStoreViewModel.saveAPIkey(apiKey)
                dataStoreViewModel.saveSettings(params = Params(purity,category,ratio,resolution))
                dataStoreViewModel.setSettingsMigrated()
            }
            /*else
            {
                Log.d("Settings","Already Migrated")
            }*/
        }

    }

    private fun Boolean.viaString() = if (this) "1" else "0"

}