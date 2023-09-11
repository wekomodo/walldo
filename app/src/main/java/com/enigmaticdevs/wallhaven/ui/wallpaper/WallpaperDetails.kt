package com.enigmaticdevs.wallhaven.ui.wallpaper

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.databinding.ActivityWallpaperDetailsBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.fragments.BottomSheetFragment
import com.enigmaticdevs.wallhaven.util.Status
import com.enigmaticdevs.wallhaven.util.download.AndroidDownloader
import com.enigmaticdevs.wallhaven.util.download.fileExists
import com.enigmaticdevs.wallhaven.util.download.getUriForPhoto
import com.enigmaticdevs.wallhaven.util.download.showFileExistsDialog
import com.enigmaticdevs.wallhaven.util.shareIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class WallpaperDetails : AppCompatActivity() {
    private val imageViewModel: MainViewModel by viewModels()
    private lateinit var imageId: String
    private lateinit var binding: ActivityWallpaperDetailsBinding
    private lateinit var photo: Photo
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var fileName: String
    private var action: String = ""
    private var downloadID = -1L
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageId = intent.getStringExtra("imageId").toString()
        Log.d("imageId", imageId)
        fileName = "walldo-${imageId}.jpg"
        initializePermissionLauncher()
        updateOrRequestPermissions()
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
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
        binding.setAsWallpaper.setOnClickListener {
            val preference = PreferenceManager.getDefaultSharedPreferences(this)
            val set_as_wallpaper = preference.getString("set_as_wallpaper", "walldo")
            when (set_as_wallpaper) {
                "walldo" -> {
                    Intent(this, SetAsWallpaper::class.java).apply {
                        putExtra(SetAsWallpaper.EXTRA_PHOTO_URL, photo.data.path)
                        startActivity(this)
                    }
                }

                "system" -> {
                    setWallpaper()
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

                R.id.download -> {
                    if (fileExists(fileName)) {
                        showFileExistsDialog(this) {
                            download("download")
                        }
                    } else {
                        download("download")
                    }
                    true
                }

                R.id.share -> shareIntent(this, photo.data.path)
                else -> true
            }

        }
    }

    private fun initializePermissionLauncher() {
        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
                writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: writePermissionGranted
            }
    }

    private fun setWallpaper() {
        if (fileExists(fileName)) {
            getUriForPhoto(fileName)?.let { uri ->
                applyWallpaper(uri)
            } ?: run {
                download("wallpaper")
            }
        } else
            download("wallpaper")
    }

    private fun updateOrRequestPermissions() {
        val hasReadPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        else
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = checkSelfPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        readPermissionGranted = hasReadPermission
        val minSdk28 = Build.VERSION.SDK_INT > Build.VERSION_CODES.P
        writePermissionGranted = hasWritePermission || minSdk28

        val permissionToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                permissionToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            else
                permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionToRequest.toTypedArray())
        }
    }

    private fun download(s: String) {
        if (readPermissionGranted && writePermissionGranted) {
            Toast.makeText(this, "Download Started", Toast.LENGTH_SHORT).show()
            val downloader = AndroidDownloader(this)

            when (s) {
                "wallpaper" -> {
                    action = s
                    downloadID = downloader.downloadWallpaper(photo.data.path, fileName)
                }

                "download" -> {
                    action = s
                    downloadID = downloader.downloadFile(photo.data.path, fileName)
                }
            }

        }
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(this@WallpaperDetails, "Download Completed", Toast.LENGTH_SHORT)
                    .show()
                when (action) {
                    "wallpaper" -> {
                        setWallpaper()
                    }

                }
            }
        }
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

    private fun applyWallpaper(uri: Uri) {
        try {
            startActivity(WallpaperManager.getInstance(this).getCropAndSetWallpaperIntent(uri))
        } catch (e: IllegalArgumentException) {
            e.localizedMessage?.let { Log.d("exception", it) }
            var bitmap: Bitmap? = null
            try {
                bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                WallpaperManager.getInstance(this).setBitmap(bitmap)
                Log.d("WallpaperIntent", "Wallpaper set successfully")
            } catch (e: Exception) {
                Log.d("WallpaperIntent", "Error setting wallpaper")
            } finally {
                bitmap?.recycle()
            }
        }
    }
}
