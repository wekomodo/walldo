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
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.databinding.ActivityWallpaperDetailsBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.fragments.BottomSheetFragment
import com.enigmaticdevs.wallhaven.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WallpaperDetails : AppCompatActivity() {
    private val imageViewModel: MainViewModel by viewModels()
    private lateinit var imageId: String
    private lateinit var binding: ActivityWallpaperDetailsBinding
    private lateinit var photo: Photo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageId = intent.getStringExtra("imageId").toString()
        Log.d("imageId", imageId)
        imageViewModel.getWallpaper(imageId)
        lifecycleScope.launch {
            imageViewModel.wallpaper.collectLatest { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        photo = response.data as Photo
                        loadImage(photo)
                    }

                    Status.ERROR -> Toast.makeText(
                        this@WallpaperDetails,
                        "Failed",
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> {}
                }
            }
        }

        binding.toolbar3.setNavigationOnClickListener {
            finish()
        }

        binding.toolbar3.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.info -> {
                    val bottomSheetFragment = BottomSheetFragment.newInstance(photo)
                    bottomSheetFragment.show(supportFragmentManager, "BottomSheetDialog")
                    true

                }
                R.id.download -> download()
                R.id.share -> share()
                else -> true
            }

        }
    }

    private fun share(): Boolean {
        return true
    }

    private fun download(): Boolean {
                     //todo
        return true
    }

    private fun loadImage(photo: Photo) {
        photo?.let { wallpaper ->
            binding.wallpaperDetailAvatarUsername.text = wallpaper.data.uploader.username
            Glide.with(this)
                .load(wallpaper.data.path)
                .placeholder(ColorDrawable(Color.parseColor(wallpaper.data.colors[(0..4).random()])))
                .into(binding.wallpaperDetailImage)
            Glide.with(this)
                .load(wallpaper.data.uploader.avatar.`128px`)
                .placeholder(ColorDrawable(Color.parseColor(wallpaper.data.colors[(0..4).random()])))
                .into(binding.wallpaperDetailAvatarImage)
        }

    }
}