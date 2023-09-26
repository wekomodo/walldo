package com.enigmaticdevs.wallhaven.ui.autowallpaper

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.enigmaticdevs.wallhaven.autoWallpaperdb.AutoWallpaperViewModel
import com.enigmaticdevs.wallhaven.data.autowallpaper.models.AutoWallpaper
import com.enigmaticdevs.wallhaven.databinding.ActivityAutoWallpaperHistoryBinding
import com.enigmaticdevs.wallhaven.ui.adapters.AutoWallpaperHistoryItemAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AutoWallpaperHistory : AppCompatActivity() {
    private lateinit var adapter: AutoWallpaperHistoryItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: ActivityAutoWallpaperHistoryBinding
    private var wallpaperList: MutableList<AutoWallpaper> = ArrayList()
    private val imagesViewModel: AutoWallpaperViewModel by viewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutoWallpaperHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        imagesViewModel.readAllData.observe(this) {
            Log.d("AutoWallHistory", it.toString())
            it?.let {
                wallpaperList = it
                if (wallpaperList.size > 0) {
                    adapter.setData(wallpaperList)
                    binding.autoWallpaperHistoryEmpty.visibility = View.GONE
                } else
                    binding.autoWallpaperHistoryEmpty.visibility = View.VISIBLE
            }

        }

        binding.toolbar3.setNavigationOnClickListener{
            finish()
        }
    }


    private fun initRecyclerView() {
        recyclerView = binding.autoWallHistory
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = AutoWallpaperHistoryItemAdapter(this, wallpaperList)
        recyclerView.adapter = adapter

    }
}