package com.enigmaticdevs.wallhaven.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.model.Data
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.databinding.FragmentPopularBinding
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import com.enigmaticdevs.wallhaven.ui.adapters.WallpaperAdapter
import com.enigmaticdevs.wallhaven.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PopularFragment : Fragment(){
    private lateinit var context : Context
    private val imagesViewModel: MainViewModel by viewModels()
    private lateinit var binding : FragmentPopularBinding
    private lateinit var wallpaperList : MutableList<Data>
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: WallpaperAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_popular,container,false)
        context = requireActivity()
        binding = FragmentPopularBinding.bind(view)
        wallpaperList = ArrayList()
        val params = Params("toplist","111","111","1y","","")
        initRecyclerView()
        lifecycleScope.launch {

            imagesViewModel.Popularlist(params).collectLatest{
                               itemAdapter.submitData(it)
            }
            /*imagesViewModel.wallpaperListPopular.collectLatest{response ->
                when(response.status){
                    Status.SUCCESS -> {
                        wallpaperList = response.data?.data as MutableList<Data>
                        Log.d("data",wallpaperList.toString())
                        initRecyclerView()
                    }
                    Status.ERROR ->
                        Toast.makeText(context,"Failed", Toast.LENGTH_SHORT).show()
                    else ->{
                    }
                }

            }*/
        }

        // MutableLiveData Example
        /*
                imagesViewModel.getSearchWallpapers("","toplist","111","111","1y","","",1)
                imagesViewModel.wallpaperSearchList.observe(this){response ->
                    if(response == null){
                        Toast.makeText(this@MainActivity,"Failed",Toast.LENGTH_SHORT).show()
                        return@observe
                    }
                    else{
                        wallpaperList = response.data as MutableList<Data>
                        initRecyclerView()

                    }
                    Log.d("data",response.toString())
                }*/

        return view
    }

    private fun initRecyclerView() {
        recyclerView = binding.recyclerview
        val  staggeredGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        recyclerView.layoutManager = staggeredGridLayoutManager
        itemAdapter = WallpaperAdapter(context)
        recyclerView.adapter = itemAdapter
    }
}