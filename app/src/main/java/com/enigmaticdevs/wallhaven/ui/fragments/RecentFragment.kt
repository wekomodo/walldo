package com.enigmaticdevs.wallhaven.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.databinding.FragmentRecentBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.adapters.LoadMoreAdapter
import com.enigmaticdevs.wallhaven.ui.adapters.WallpaperAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecentFragment : Fragment() {
    private lateinit var context: Context
    private val imagesViewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentRecentBinding
    private lateinit var wallpaperList: MutableList<Wallpaper>
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: WallpaperAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recent, container, false)
        context = requireActivity()
        binding = FragmentRecentBinding.bind(view)
        wallpaperList = ArrayList()
        val params = Params("date_added", "100", "111", "1y", "", "")
        initRecyclerView()
        loadData(params)
        initErrorHandling()
        binding.retryLoading.setOnClickListener {
            loadData(params)
        }
        return view
    }

    private fun loadData(params: Params) {
        viewLifecycleOwner.lifecycleScope.launch {
            imagesViewModel.recentList(params).collectLatest {
                itemAdapter.submitData(it)
            }
        }
    }

    private fun initErrorHandling() {
        viewLifecycleOwner.lifecycleScope.launch {
            itemAdapter.loadStateFlow.collect {
                val state = it.refresh
                if (state is LoadState.Error)
                    binding.failedToLoad.visibility = View.VISIBLE
                if (state is LoadState.Loading)
                    binding.failedToLoad.visibility = View.GONE
            }
        }
    }


    private fun initRecyclerView() {
        recyclerView = binding.recyclerview
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        recyclerView.layoutManager = staggeredGridLayoutManager
        itemAdapter = WallpaperAdapter(context)
        recyclerView.adapter = itemAdapter.withLoadStateFooter(
            LoadMoreAdapter{
                itemAdapter.retry()
            }
        )
    }
}