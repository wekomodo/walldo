package com.enigmaticdevs.wallhaven.ui.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.databinding.ActivityWallpaperDetailsBinding
import com.enigmaticdevs.wallhaven.databinding.ItemPhotoBinding
import com.enigmaticdevs.wallhaven.ui.wallpaper.WallpaperDetails
import com.enigmaticdevs.wallhaven.util.AspectRatioImageView
import com.enigmaticdevs.wallhaven.util.setAspectRatio

class WallpaperAdapter(
    private val context: Context
) : PagingDataAdapter<Wallpaper, WallpaperAdapter.ViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Wallpaper>() {
            override fun areItemsTheSame(oldItem: Wallpaper, newItem: Wallpaper): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Wallpaper, newItem: Wallpaper): Boolean {
                return oldItem == newItem
            }

        }
    }

    inner class ViewHolder(binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        val photo: AspectRatioImageView = binding.photo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val vh = ViewHolder(inflatedView)
        vh.photo.setOnClickListener {
            val intent = Intent(context, WallpaperDetails::class.java)
            intent.putExtra("imageId", getItem(vh.absoluteAdapterPosition)?.id)
            context.startActivity(intent)
        }
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { currentItem ->
            holder.photo.setAspectRatio(currentItem.dimension_x, currentItem.dimension_y)
            Glide.with(context)
                .load(currentItem.thumbs.original)
                .placeholder(ColorDrawable(Color.parseColor(currentItem.colors[(0..4).random()])))
                .into(holder.photo)
        }
    }
}