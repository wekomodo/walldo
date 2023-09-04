package com.enigmaticdevs.wallhaven.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Wallpaper
import com.enigmaticdevs.wallhaven.databinding.FragmentPopularBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.DataStoreViewModel
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.adapters.LoadMoreAdapter
import com.enigmaticdevs.wallhaven.ui.adapters.WallpaperAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PopularFragment : Fragment() {
    private lateinit var context: Context
    private val imagesViewModel: MainViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private lateinit var binding: FragmentPopularBinding
    private lateinit var wallpaperList: MutableList<Wallpaper>
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: WallpaperAdapter
    private lateinit var params: Params
    private var searchJob: Job? = null
    private var topRange = "1y"
    private var sorting = "toplist"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_popular, container, false)
        context = requireActivity()
        binding = FragmentPopularBinding.bind(view)
        wallpaperList = ArrayList()
        params = Params("100", "111", "", "")
        initRecyclerView()
        initErrorHandling()
        searchJob = loadData()
        dataStoreViewModel.readSettings()
        dataStoreViewModel.settings.observe(viewLifecycleOwner) {
            if(it.purity != "null"){
                params = it
                Log.d("paramsPopular", params.toString())
                searchJob?.cancel()
                searchJob = loadData()
            }
        }
        binding.retryLoading.setOnClickListener {
           itemAdapter.retry()
        }
        return view
    }


    private fun loadData(): Job {
        return viewLifecycleOwner.lifecycleScope.launch {
            imagesViewModel.popularList(sorting,topRange, params).collectLatest {
                itemAdapter.submitData(it)
            }
        }
    }

    private fun initErrorHandling() {
        viewLifecycleOwner.lifecycleScope.launch {
            itemAdapter.loadStateFlow.collect {
                val state = it.refresh
                binding.apply {
                    if (state is LoadState.Error)
                        failedToLoad.visibility = View.VISIBLE
                    if (state is LoadState.Loading)
                        failedToLoad.visibility = View.GONE
                }
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
        val footerAdapter = LoadMoreAdapter { itemAdapter.retry() }
        recyclerView.adapter = itemAdapter.withLoadStateFooter(footerAdapter)
    }
}