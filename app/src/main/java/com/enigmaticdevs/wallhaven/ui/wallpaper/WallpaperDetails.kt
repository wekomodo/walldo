package com.enigmaticdevs.wallhaven.ui.wallpaper

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.databinding.ActivityWallpaperDetailsBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WallpaperDetails : AppCompatActivity() {
    private val imageViewModel : MainViewModel by viewModels()
    private lateinit var imageId : String
    private lateinit var binding: ActivityWallpaperDetailsBinding
    private lateinit var wallpaper : Photo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageId = intent.getStringExtra("imageId").toString()
        Log.d("imageId",imageId)
        imageViewModel.getWallpaper(imageId)
        lifecycleScope.launch {
            imageViewModel.wallpaper.collectLatest {response ->
                when(response.status){
                    Status.SUCCESS -> {
                        wallpaper = response.data as Photo
                        loadImage(wallpaper)
                    }
                    Status.ERROR -> Toast.makeText(this@WallpaperDetails,"Failed", Toast.LENGTH_SHORT).show()
                    else ->{}
                    }
            }
        }

        binding.toolbar3.setNavigationOnClickListener{
            finish()
        }
    }

    private fun loadImage(wallpaper: Photo) {
        binding.wallpaperDetailAvatarUsername.text = wallpaper.wallpaper.uploader.username
        Glide.with(this)
            .load(wallpaper.wallpaper.path)
            .placeholder(ColorDrawable(Color.parseColor(wallpaper.wallpaper.colors[(0..4).random()])))
            .into(binding.wallpaperDetailImage)
        Glide.with(this)
            .load(wallpaper.wallpaper.uploader.avatar.`128px`)
            .placeholder(ColorDrawable(Color.parseColor(wallpaper.wallpaper.colors[(0..4).random()])))
            .into(binding.wallpaperDetailAvatarImage)
    }
}