package com.enigmaticdevs.wallhaven

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.enigmaticdevs.wallhaven.databinding.ActivityMainBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.adapters.ViewPagerFragmentAdapter
import com.enigmaticdevs.wallhaven.ui.fragments.PopularFragment
import com.enigmaticdevs.wallhaven.ui.fragments.RecentFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val imagesViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavDrawer(binding)
        binding.fragmentContainer.offscreenPageLimit = 2
        initFragmentAdapter(binding)
    }

    private fun initNavDrawer(binding: ActivityMainBinding) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_settings -> Toast.makeText(this, "Item 1", Toast.LENGTH_SHORT).show()
                R.id.nav_autowallpaper -> Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()
                else -> {
                    Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else
                    onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    private fun initFragmentAdapter(binding: ActivityMainBinding) {
        val adapter = ViewPagerFragmentAdapter(this)
        adapter.addFragment(PopularFragment(), "Popular")
        adapter.addFragment(RecentFragment(), "Recent")
        binding.fragmentContainer.adapter = adapter
        binding.popularButton.setOnClickListener{
            binding.fragmentContainer.currentItem = 0
        }
        binding.recentsButton.setOnClickListener{
            binding.fragmentContainer.currentItem = 1
        }
        binding.fragmentContainer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
            override fun onPageSelected(position: Int) {
                if(position == 0)
                {
                    binding.popularButtonText.visibility = View.VISIBLE
                    binding.recentButtonText.visibility = View.GONE
                }
                else {
                    binding.recentButtonText.visibility = View.VISIBLE
                    binding.popularButtonText.visibility = View.GONE
                }
            }
        })
    }


}