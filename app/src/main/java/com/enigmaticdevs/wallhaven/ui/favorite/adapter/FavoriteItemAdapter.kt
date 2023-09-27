package com.enigmaticdevs.wallhaven.ui.favorite.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.favorite.models.FavoriteImages
import com.enigmaticdevs.wallhaven.ui.wallpaper.WallpaperDetails
import com.enigmaticdevs.wallhaven.util.customToast
import com.enigmaticdevs.wallhaven.util.imageview.AspectRatioImageView
import com.enigmaticdevs.wallhaven.util.imageview.setAspectRatio

class FavoriteItemAdapter(private var list: MutableList<FavoriteImages>, private val context: Context) : RecyclerView.Adapter<FavoriteItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: AspectRatioImageView = itemView.findViewById(R.id.photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_photo,
            parent,
            false
        )
        val vh = ViewHolder(inflatedView)
        vh.photo.setOnClickListener {
            val position = vh.absoluteAdapterPosition
            val id = list[position].imageId
            val intent = Intent(context, WallpaperDetails::class.java)
            intent.putExtra("imageId", id)
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val apiKey = preferences.getString("api_key", "").toString()
            if(list[position].purity == "nsfw")
            {
                if(apiKey.isNotEmpty())
                    context.startActivity(intent)
                else
                    customToast(context,"No API key Found!")
            }
            if(list[position].purity != "nsfw" ){
                context.startActivity(intent)
            }
        }
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.photo.setAspectRatio(list[position].dimension_x, list[position].dimension_y)
            Glide.with(context)
                .load(list[position].thumbs)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(holder.photo)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun setData(image : MutableList<FavoriteImages>)
    {
         list = image
         notifyDataSetChanged()
    }
}
