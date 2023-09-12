package com.enigmaticdevs.wallhaven.ui.search

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.databinding.ActivitySearchBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.adapters.LoadMoreAdapter
import com.enigmaticdevs.wallhaven.ui.adapters.WallpaperAdapter
import com.enigmaticdevs.wallhaven.util.customToast
import com.enigmaticdevs.wallhaven.util.errorToast
import com.enigmaticdevs.wallhaven.util.focusAndShowKeyboard
import com.enigmaticdevs.wallhaven.util.hideKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: WallpaperAdapter
    private var searchJob: Job? = null
    private var tag: String? = null
    private val params: Params = Params("110", "111", "", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tag = intent.getStringExtra("tag")
        initRecyclerView()
        initErrorHandling()
        tag?.let {
            val query = "+$it"
            binding.searchTextEditText.setText(it)
            searchJob = loadData(query)
            binding.searchTextEditText.isEnabled = false
            binding.searchInfoFab.isVisible = false
        } ?: run{
            binding.searchTextEditText.focusAndShowKeyboard()
            binding.searchTextEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = binding.searchTextEditText.text.toString()
                    searchJob = loadData(query)
                    currentFocus?.hideKeyboard()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            searchInfo()
        }
        binding.materialToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadData(query: String): Job {
        return lifecycleScope.launch {
            viewModel.searchList(query, "relevance", "", params).collectLatest {
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
                        errorToast(this@SearchActivity)
                    }
                    if (it.append.endOfPaginationReached && state is LoadState.NotLoading) {
                        if (itemAdapter.itemCount < 1)
                            customToast(this@SearchActivity, "No results found")
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

    private fun searchInfo() {
        binding.searchInfoFab.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Ways of finding what you're looking for")
                .setMessage(
                    "• tagname (Search fuzzily for a tag) \n" +
                            "• -tagname (exludes a tag/keyword)\n" +
                            "• +tag1+tag2 (must have tag1 and tag2)\n" +
                            "• @username (useruploads)\n" +
                            "• id:123 (Exact tag search(not ImageID))\n" +
                            "• like:wallpaper ID (Find wallpapers with similar tags)"
                )
                .setPositiveButton("Ok") { _, _ ->
                }
                .show()
        }
    }
}