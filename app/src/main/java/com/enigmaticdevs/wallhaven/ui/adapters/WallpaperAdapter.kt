package com.enigmaticdevs.wallhaven.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enigmaticdevs.wallhaven.databinding.ItemPhotoBinding
import com.enigmaticdevs.wallhaven.response.Data
import com.enigmaticdevs.wallhaven.util.AspectRatioImageView
import com.enigmaticdevs.wallhaven.util.setAspectRatio

class WallpaperAdapter(
    private val wallpaper: MutableList<Data>,
    private val context : Context ) : RecyclerView.Adapter<WallpaperAdapter.ViewHolder>() {
    class ViewHolder(binding : ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        val photo: AspectRatioImageView = binding.photo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val vh = ViewHolder(inflatedView)
        return vh
    }

    override fun getItemCount(): Int {
        return wallpaper.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.photo.setAspectRatio(wallpaper[position].dimension_x, wallpaper[position].dimension_y)
        Glide.with(context)
            .load(wallpaper[position].thumbs.original)
            .placeholder(ColorDrawable(Color.parseColor(wallpaper[position].colors[(0..4).random()])))
            .into(holder.photo)
    }
}