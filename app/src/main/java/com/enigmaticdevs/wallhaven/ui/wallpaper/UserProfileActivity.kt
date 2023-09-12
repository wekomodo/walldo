package com.enigmaticdevs.wallhaven.ui.wallpaper

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.databinding.ActivityUserProfileBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.adapters.LoadMoreAdapter
import com.enigmaticdevs.wallhaven.ui.adapters.WallpaperAdapter
import com.enigmaticdevs.wallhaven.util.customToast
import com.enigmaticdevs.wallhaven.util.errorToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUserProfileBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: WallpaperAdapter
    private var searchJob: Job? = null
    private var username : String? = null
    private var userImage : String? = null
    private val params: Params = Params("111", "111", "", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        initErrorHandling()
        username  = intent.getStringExtra("username")
        userImage  = intent.getStringExtra("userImage")
        userImage?.let {
            Glide.with(this)
                .load(it)
                .into(binding.userProfileAvatarImage)
        }
        username?.let{
            val query = "@$username"
            binding.userProfileAvatarUsername.text = username
            searchJob = loadData(query)
        }
        binding.userProfileToolbar.setNavigationOnClickListener{
            finish()
        }
    }

    private fun loadData(q : String): Job {
        return lifecycleScope.launch {
            viewModel.searchList(q, "relevance", "", params).collectLatest {
                itemAdapter.submitData(it)
            }
        }
    }
    private fun initErrorHandling() {
        lifecycleScope.launch {
            itemAdapter.loadStateFlow.collect {
                val state = it.refresh
                binding.apply {
                    if (state is LoadState.Error) {
                        errorToast(this@UserProfileActivity)
                    }
                    if (it.append.endOfPaginationReached && state is LoadState.NotLoading) {
                        if ( itemAdapter.itemCount < 1)
                            customToast(this@UserProfileActivity,"No results found")
                        ///  hide empty view

                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        recyclerView = binding.recyclerView
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )

        recyclerView.layoutManager = staggeredGridLayoutManager
        itemAdapter = WallpaperAdapter(this)
        val footerAdapter = LoadMoreAdapter { itemAdapter.retry() }
        recyclerView.adapter = itemAdapter.withLoadStateFooter(footerAdapter)
    }


}