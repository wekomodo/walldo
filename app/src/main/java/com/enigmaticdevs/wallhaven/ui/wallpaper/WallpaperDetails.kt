package com.enigmaticdevs.wallhaven.ui.wallpaper

import android.Manifest
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Photo
import com.enigmaticdevs.wallhaven.databinding.ActivityWallpaperDetailsBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.fragments.BottomSheetFragment
import com.enigmaticdevs.wallhaven.util.Status
import com.enigmaticdevs.wallhaven.util.download.AndroidDownloader
import com.enigmaticdevs.wallhaven.util.download.DATA_ACTION
import com.enigmaticdevs.wallhaven.util.download.DATA_URI
import com.enigmaticdevs.wallhaven.util.download.DOWNLOAD_STATUS
import com.enigmaticdevs.wallhaven.util.download.DownloadAction
import com.enigmaticdevs.wallhaven.util.download.STATUS_FAILED
import com.enigmaticdevs.wallhaven.util.download.STATUS_SUCCESSFUL
import com.enigmaticdevs.wallhaven.util.download.fileExists
import com.enigmaticdevs.wallhaven.util.download.getUriForPhoto
import com.enigmaticdevs.wallhaven.util.download.showFileExistsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.Serializable


@AndroidEntryPoint
class WallpaperDetails : AppCompatActivity() {
    private val imageViewModel: MainViewModel by viewModels()
    private lateinit var imageId: String
    private lateinit var binding: ActivityWallpaperDetailsBinding
    private lateinit var photo: Photo
    private var downloadReceiver: BroadcastReceiver? = null
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var fileName: String
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageId = intent.getStringExtra("imageId").toString()
        Log.d("imageId", imageId)
        fileName = "walldo-${imageId}.jpg"
        initializePermissionLauncher()
        updateOrRequestPermissions()
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
            //setWallpaper()
            Intent(this, SetAsWallpaper::class.java).apply {
                putExtra(SetAsWallpaper.EXTRA_PHOTO_URL, photo.data.path)
                startActivity(this)
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
                            download()
                        }
                    } else {
                        download()
                    }
                    true
                }

                R.id.share -> share()
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
                download()
            }
        } else
            download()
    }

    private fun updateOrRequestPermissions() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT > Build.VERSION_CODES.Q
        val minSdk32 = Build.VERSION.SDK_INT > Build.VERSION_CODES.S
        writePermissionGranted = hasWritePermission || minSdk32
        readPermissionGranted = hasReadPermission || minSdk29

        val permissionToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionToRequest.toTypedArray())
        }
    }

    private fun share(): Boolean {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody =
            "Here's an awesome wallpaper I found on Walldo " + photo.data.path
        val shareSub = R.string.app_name.toString()
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share using"))
        return true
    }

    private fun download() {
        /*if (readPermissionGranted && writePermissionGranted) {*/
            Toast.makeText(this,"Download Started",Toast.LENGTH_SHORT).show()
            val downloader = AndroidDownloader(this)
            val downloadID =  downloader.downloadFile(photo.data.path,fileName)
       /* }*/
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
   /* private fun getSerializable(): DownloadAction {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra(DATA_ACTION, DownloadAction::class.java) as DownloadAction
        else
            intent.getSerializableExtra(DATA_ACTION) as DownloadAction
    }

    private fun getParcelable(): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(DATA_URI, Uri::class.java) as Uri
        else
            intent.getParcelableExtra(DATA_URI) as Uri?
    }
*/
/*
    override fun onStart() {
        super.onStart()
        downloadReceiver = registerReceiver(IntentFilter(ACTION_DOWNLOAD_COMPLETE)) {
            it?.let { handleDownloadIntent(it) }
        }
    }

    override fun onStop() {
        super.onStop()
        downloadReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
    }
*/

    private fun handleDownloadIntent(intent: Intent) {
        val action = getSerializable(intent, DATA_ACTION, DownloadAction::class.java)
        val status = intent.getIntExtra(DOWNLOAD_STATUS, -1)

        if (action == DownloadAction.WALLPAPER) {
            when (status) {
                STATUS_SUCCESSFUL -> getParcelable(intent, DATA_URI, Uri::class.java)?.let {
                    applyWallpaper(it)
                }
                STATUS_FAILED -> {
                   Toast.makeText(this,"Download Failed",Toast.LENGTH_SHORT).show()
                }
            }
        } else if (action == DownloadAction.DOWNLOAD) {
            when (status) {
                STATUS_SUCCESSFUL -> Toast.makeText(this,"Download Successful",Toast.LENGTH_SHORT).show()
                STATUS_FAILED -> Toast.makeText(this,"Download Failed",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun <T : Serializable?> getSerializable(intent: Intent, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra(name, clazz)!!
        else
            intent.getSerializableExtra(name) as T
    }
    private fun <T: Parcelable?> getParcelable(intent: Intent,name: String,clazz : Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(name, clazz)
        else
            intent.getParcelableExtra(DATA_URI)
    }

}
