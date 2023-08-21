package com.enigmaticdevs.wallhaven

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.enigmaticdevs.wallhaven.databinding.ActivityMainBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.data.model.Data
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.ui.adapters.ViewPagerFragmentAdapter
import com.enigmaticdevs.wallhaven.ui.adapters.WallpaperAdapter
import com.enigmaticdevs.wallhaven.ui.fragments.PopularFragment
import com.enigmaticdevs.wallhaven.ui.fragments.RecentFragment
import com.enigmaticdevs.wallhaven.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val imagesViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fragmentContainer.offscreenPageLimit = 2
        initFragmentAdapter(binding)
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