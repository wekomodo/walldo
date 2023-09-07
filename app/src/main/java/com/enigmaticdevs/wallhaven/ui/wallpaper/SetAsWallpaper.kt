package com.enigmaticdevs.wallhaven.ui.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.enigmaticdevs.wallhaven.databinding.ActivitySetAsWallpaperBinding
import com.enigmaticdevs.wallhaven.databinding.SetWallpaperDailogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SetAsWallpaper : AppCompatActivity() {
    private lateinit var binding: ActivitySetAsWallpaperBinding
    private var isSystemUiVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetAsWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.post {
            toggleSystemUi(false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
                val suppliedInsets = view.onApplyWindowInsets(windowInsets)
                isSystemUiVisible = suppliedInsets.isVisible(
                    WindowInsetsCompat.Type.statusBars()
                            or WindowInsetsCompat.Type.navigationBars()
                )
                suppliedInsets
            }
        } else {
            window.decorView.setOnSystemUiVisibilityChangeListener {
                isSystemUiVisible = (it and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
            }
        }
        val url = intent.getStringExtra(EXTRA_PHOTO_URL)
        url?.let {
            Glide.with(this)
                .load(url)
                .into(binding.setAsWallpaperImage)
        } ?: run {
            Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.setAsWallpaperCropButton.setOnClickListener {
            binding.setAsWallpaperCropButton.visibility = View.INVISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                getScreenShotFromView(binding.setAsWallpaperCard, this@SetAsWallpaper)
                {
                    val manager = WallpaperManager.getInstance(this@SetAsWallpaper)
                    val bitmap = it
                    val dialogBinding =
                        SetWallpaperDailogBinding.inflate(LayoutInflater.from(this@SetAsWallpaper))
                    val view = dialogBinding.root
                    val dialog = MaterialAlertDialogBuilder(this@SetAsWallpaper)
                        .setView(view)
                        .show()

                    dialogBinding.setAsBoth.setOnClickListener {
                        manager.setBitmap(bitmap)
                        dialog.dismiss()
                        finish()
                    }
                    dialogBinding.setAsHomescreen.setOnClickListener {
                        manager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_SYSTEM)
                        dialog.dismiss()
                        finish()
                    }
                    dialogBinding.setAsLockscreen.setOnClickListener {
                        manager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK)
                        dialog.dismiss()
                        finish()
                    }
                    dialog.setOnDismissListener {
                        Toast.makeText(this, "Wallpaper Set", Toast.LENGTH_SHORT).show()
                        binding.setAsWallpaperCropButton.visibility = View.VISIBLE
                    }
                }
            }, 500)

        }
    }

    private fun toggleSystemUi(showSystemUi: Boolean) {
        WindowInsetsControllerCompat(window, binding.root).let {
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (showSystemUi) {
                it.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            } else {
                it.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    private fun getScreenShotFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            var bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PixelCopy.request(
                        window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + view.height
                        ), bitmap, { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap)
                            } else {
                                if (copyResult == PixelCopy.ERROR_UNKNOWN)
                                    Toast.makeText(
                                        this,
                                        "Unexpected error occurred",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                            // possible to handle other result codes ...
                        },
                        Handler(Looper.getMainLooper())
                    )
                } else {
                    val screenView = view.rootView
                    screenView.isDrawingCacheEnabled = true
                    bitmap = Bitmap.createBitmap(screenView.drawingCache)
                    screenView.isDrawingCacheEnabled = false
                    callback(bitmap)
                }
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val EXTRA_PHOTO_URL = "extra_photo_url"
    }
}