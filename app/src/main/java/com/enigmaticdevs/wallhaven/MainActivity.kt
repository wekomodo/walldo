package com.enigmaticdevs.wallhaven

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.enigmaticdevs.wallhaven.databinding.ActivityMainBinding
import com.enigmaticdevs.wallhaven.ui.settings.Settings
import com.enigmaticdevs.wallhaven.ui.adapters.ViewPagerFragmentAdapter
import com.enigmaticdevs.wallhaven.ui.fragments.PopularFragment
import com.enigmaticdevs.wallhaven.ui.fragments.RecentFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavDrawer()
        binding.fragmentContainer.offscreenPageLimit = 2
        initFragmentAdapter()
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
                R.id.nav_autowallpaper -> Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()
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


}