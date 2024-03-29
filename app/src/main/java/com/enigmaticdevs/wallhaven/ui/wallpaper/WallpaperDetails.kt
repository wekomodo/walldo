package com.enigmaticdevs.wallhaven.ui.wallpaper

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.favorite.models.FavoriteImages
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.databinding.ActivityWallpaperDetailsBinding
import com.enigmaticdevs.wallhaven.domain.favorite.FavoriteViewModel
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.fragments.BottomSheetFragment
import com.enigmaticdevs.wallhaven.util.Status
import com.enigmaticdevs.wallhaven.util.customToast
import com.enigmaticdevs.wallhaven.util.download.AndroidDownloader
import com.enigmaticdevs.wallhaven.util.download.fileExists
import com.enigmaticdevs.wallhaven.util.download.getUriForPhoto
import com.enigmaticdevs.wallhaven.util.download.showFileExistsDialog
import com.enigmaticdevs.wallhaven.util.errorToast
import com.enigmaticdevs.wallhaven.util.hasNotificationPermission
import com.enigmaticdevs.wallhaven.util.hasReadPermission
import com.enigmaticdevs.wallhaven.util.hasWritePermission
import com.enigmaticdevs.wallhaven.util.registerBroadcastReceiver
import com.enigmaticdevs.wallhaven.util.shareIntent
import com.zedlabs.pastelplaceholder.Pastel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class WallpaperDetails : AppCompatActivity() {
    private val imageViewModel: MainViewModel by viewModels()
    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private lateinit var imageId: String
    private lateinit var binding: ActivityWallpaperDetailsBinding
    private var photo: Photo? = null
    private var isChecked = false
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var fileName: String
    private var action: String = ""
    private var downloadID = -1L
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private var favoriteImage: FavoriteImages? = null
    private var favoriteImages: MutableList<FavoriteImages> = ArrayList()

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
        initOnClickListeners()
        registerBroadcastReceiver(this,onDownloadComplete)
        readDatabase()
    }

    private fun readDatabase() {
        imageViewModel.getWallpaper(imageId)
        favoriteViewModel.readAllData.observe(this) {
            favoriteImages = it
            setChecked()
        }
        lifecycleScope.launch {
            imageViewModel.wallpaper.collectLatest { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        binding.imageFailedToLoad.visibility = View.GONE
                        binding.wallpaperDetailLoadingBar.visibility = View.GONE
                        photo = response.data
                        loadImage(photo)
                    }

                    Status.ERROR -> {
                        binding.wallpaperDetailLoadingBar.visibility = View.GONE
                        binding.imageFailedToLoad.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }
        }

    }

    private fun setChecked() {
        for (item in favoriteImages) {
            if (item.imageId == imageId) {
                favoriteImage = item
                binding.toolbar3.menu[0].setIcon(R.drawable.ic_favorite_checked)
                isChecked = true
            }
        }
    }

    private fun initOnClickListeners() {
        binding.cardView.setOnClickListener {
            launchUserProfile()
        }
        binding.wallpaperDetailAvatarUsername.setOnClickListener {
            launchUserProfile()
        }
        binding.imageFailedTryAgain.setOnClickListener {
            binding.wallpaperDetailLoadingBar.visibility = View.VISIBLE
            binding.imageFailedToLoad.visibility = View.GONE
            imageViewModel.getWallpaper(imageId)
        }
        binding.setAsWallpaper.setOnClickListener {
            photo?.let {
                val preference = PreferenceManager.getDefaultSharedPreferences(this)
                when (preference.getString("set_as_wallpaper", "walldo")) {
                    "walldo" -> {
                        Intent(this, SetAsWallpaper::class.java).apply {
                            putExtra(SetAsWallpaper.EXTRA_PHOTO_URL, it.data.path)
                            startActivity(this)
                        }
                    }

                    "system" -> {
                        setWallpaper()
                    }
                }
            }
        }
        binding.toolbar3.setNavigationOnClickListener {
            finish()
        }
        binding.toolbar3.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.info -> {
                    photo?.let { item ->
                        val bottomSheetFragment = BottomSheetFragment.newInstance(item)
                        bottomSheetFragment.show(supportFragmentManager, "BottomSheetDialog")
                    } ?: run {
                        errorToast(this@WallpaperDetails)
                    }

                    true

                }

                R.id.download -> {
                    photo?.let {
                        if (fileExists(fileName)) {
                            showFileExistsDialog(this) {
                                download("download")
                            }
                        } else
                            download("download")
                    } ?: run {
                        errorToast(this@WallpaperDetails)
                    }
                    true
                }

                R.id.share -> {
                    photo?.let { item ->
                        shareIntent(this, item.data.path)
                    } ?: run {
                        errorToast(this@WallpaperDetails)
                    }
                    true
                }

                R.id.favorite -> {
                    if (!isChecked) {
                        isChecked = true
                        addToFavorites()
                        it.setIcon(R.drawable.ic_favorite_checked)
                        customToast(this@WallpaperDetails, "Added to favorites")
                    } else {
                        isChecked = false
                        deleteFromFavorites()
                        it.setIcon(R.drawable.ic_favorite_unchecked)
                        customToast(this@WallpaperDetails, "Removed from favorites")
                    }

                    true
                }

                else -> true
            }

        }
    }

    private fun addToFavorites() {
        photo?.let {
            val image = FavoriteImages(
                0,
                imageId,
                it.data.thumbs.original,
                it.data.path,
                it.data.purity,
                it.data.dimension_x,
                it.data.dimension_y
            )
            favoriteViewModel.addImage(image)
        }
    }

    private fun deleteFromFavorites() {


        favoriteImage?.let {
            favoriteViewModel.deleteImage(it)
        }
    }

    private fun launchUserProfile() {
        photo?.let {
            val username = it.data.uploader.username
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("userImage", it.data.uploader.avatar.`128px`)
            intent.putExtra("username", username)
            startActivity(intent)
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
        val hasReadPermission = hasReadPermission()
        val hasWritePermission = hasWritePermission()
        readPermissionGranted = hasReadPermission
        val minSdk28 = Build.VERSION.SDK_INT > Build.VERSION_CODES.P
        writePermissionGranted = hasWritePermission || minSdk28
        val notificationPermissionGranted = hasNotificationPermission()
        val permissionToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!notificationPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
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
        photo?.let { item ->
            if (readPermissionGranted && writePermissionGranted) {
                customToast(this@WallpaperDetails, "Download Started")
                val downloader = AndroidDownloader(this)

                when (s) {
                    "wallpaper" -> {
                        action = s
                        downloadID = downloader.downloadWallpaper(item.data.path, fileName)
                    }

                    "download" -> {
                        action = s
                        downloadID = downloader.downloadFile(item.data.path, fileName)
                    }
                }

            }
        } ?: run {
            errorToast(this@WallpaperDetails)
        }

    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                customToast(this@WallpaperDetails, "Download Finished")
                when (action) {
                    "wallpaper" -> {
                        setWallpaper()
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(onDownloadComplete)
        super.onDestroy()
    }

    private fun loadImage(photo: Photo?) {
        photo?.let { wallpaper ->
            binding.wallpaperDetailAvatarUsername.text = wallpaper.data.uploader.username
            Glide.with(this)
                .load(wallpaper.data.path)
                .placeholder(Pastel.getColorLight())
                .into(binding.wallpaperDetailImage)
            Glide.with(this)
                .load(wallpaper.data.uploader.avatar.`128px`)
                .placeholder(Pastel.getColorLight())
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
