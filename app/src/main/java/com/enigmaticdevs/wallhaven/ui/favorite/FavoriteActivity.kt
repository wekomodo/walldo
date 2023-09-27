package com.enigmaticdevs.wallhaven.ui.favorite

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.enigmaticdevs.wallhaven.data.favorite.models.FavoriteImages
import com.enigmaticdevs.wallhaven.databinding.ActivityFavoriteBinding
import com.enigmaticdevs.wallhaven.domain.favorite.FavoriteViewModel
import com.enigmaticdevs.wallhaven.ui.favorite.adapter.FavoriteItemAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavoriteActivity : AppCompatActivity() {
    private lateinit var adapter: FavoriteItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var staggeredGridLayoutManager: StaggeredGridLayoutManager
    private  var wallpaperList : MutableList<FavoriteImages> = ArrayList()
    private lateinit var binding : ActivityFavoriteBinding
    private val favoriteViewModel : FavoriteViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        favoriteViewModel.readAllData.observe(this){
            adapter.setData(it)
            if (it.size > 0) {
                binding.favoriteEmpty.visibility = View.GONE
            } else
                binding.favoriteEmpty.visibility = View.VISIBLE
        }
        binding.materialToolbar5.setNavigationOnClickListener{
            finish()
        }
    }

    private fun initRecyclerView() {
        recyclerView = binding.recyclerView
        staggeredGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        recyclerView.layoutManager = staggeredGridLayoutManager
        adapter = FavoriteItemAdapter(wallpaperList, this)
        recyclerView.adapter = adapter
    }
}