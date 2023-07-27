package com.enigmaticdevs.wallhaven

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.enigmaticdevs.wallhaven.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val imagesViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imagesViewModel.getWallpapers("black","date_added","111","001","1y",1)
        imagesViewModel.wallpaperList.observe(this){response ->
            if(response == null){
                 Toast.makeText(this@MainActivity,"Failed",Toast.LENGTH_SHORT).show()
                return@observe
            }
            Log.d("data",response.toString())
        }
        // BLEH !

    }
}