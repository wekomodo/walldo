package com.enigmaticdevs.wallhaven.util

import android.content.Context
import android.content.Intent
import com.enigmaticdevs.wallhaven.R

fun shareIntent(context : Context, path  : String) : Boolean{
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    val shareBody = "Here's an awesome wallpaper I found on Walldo $path"
    val shareSub = R.string.app_name.toString()
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
    context.startActivity(Intent.createChooser(sharingIntent, "Share using"))
    return true
}