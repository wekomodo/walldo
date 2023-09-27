package com.enigmaticdevs.wallhaven.ui.autowallpaper

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.autoWallpaperdb.AutoWallpaperViewModel
import com.enigmaticdevs.wallhaven.data.autowallpaper.models.AutoWallpaper
import com.enigmaticdevs.wallhaven.databinding.ActivityAutoWallpaperHistoryBinding
import com.enigmaticdevs.wallhaven.ui.adapters.AutoWallpaperHistoryItemAdapter
import com.enigmaticdevs.wallhaven.util.customToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        binding.toolbar3.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.delete_all -> deleteAllDialog()
                R.id.auto_wall_history_info -> customToast(this,"Long click to delete single item")
                else -> {}
            }
            true
        }
    }

    private fun deleteAllDialog() {
        if(wallpaperList.size>0) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete All")
                .setMessage("This will delete all list?")
                .setPositiveButton("Yes") { _, _ ->
                    imagesViewModel.deleteAll()
                    wallpaperList.clear()
                    adapter.setData(wallpaperList)
                }.setNegativeButton("No",null)
                .show()

        }
        else
            customToast(this,"Nothing to delete")


    }


    private fun initRecyclerView() {
        recyclerView = binding.autoWallHistory
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = AutoWallpaperHistoryItemAdapter(this, wallpaperList)
        recyclerView.adapter = adapter

    }
}