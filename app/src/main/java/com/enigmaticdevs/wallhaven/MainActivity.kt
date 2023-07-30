package com.enigmaticdevs.wallhaven

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.enigmaticdevs.wallhaven.databinding.ActivityMainBinding
import com.enigmaticdevs.wallhaven.main.MainViewModel
import com.enigmaticdevs.wallhaven.response.Data
import com.enigmaticdevs.wallhaven.response.Wallpaper
import com.enigmaticdevs.wallhaven.ui.adapters.WallpaperAdapter
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.notify

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val imagesViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: WallpaperAdapter
    private lateinit var wallpaperList : MutableList<Data>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wallpaperList = ArrayList()
        imagesViewModel.listWallpapers("toplist","111","111","1y",1)
        imagesViewModel.wallpaperList.observe(this){response ->
            if(response == null){
                 Toast.makeText(this@MainActivity,"Failed",Toast.LENGTH_SHORT).show()
                return@observe
            }
            else{
                wallpaperList = response.data as MutableList<Data>
                initRecyclerView()

            }
            Log.d("data",response.toString())
        }

    }

    private fun initRecyclerView() {
        recyclerView = binding.recyclerview
        val  staggeredGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        recyclerView.layoutManager = staggeredGridLayoutManager
        itemAdapter = WallpaperAdapter(wallpaperList, this)
        recyclerView.adapter = itemAdapter
    }
}