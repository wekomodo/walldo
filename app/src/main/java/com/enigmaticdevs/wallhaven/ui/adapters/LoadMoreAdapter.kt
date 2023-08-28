package com.enigmaticdevs.wallhaven.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.enigmaticdevs.wallhaven.databinding.LoadMoreBinding

class LoadMoreAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadMoreAdapter.ViewHolder>() {

    private lateinit var binding: LoadMoreBinding

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        binding = LoadMoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(retry)
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.setData(loadState)
    }

    inner class ViewHolder(retry: () -> Unit) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnLoadMoreRetry.setOnClickListener { retry() }
        }

        fun setData(state: LoadState) {
            binding.apply {
                prgBarLoadMore.isVisible = state is LoadState.Loading
                btnLoadMoreRetry.isVisible = state is LoadState.Error
                if(state is LoadState.Loading)
                btnLoadMoreRetry.visibility = View.GONE

            }
        }
    }
}