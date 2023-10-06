package com.enigmaticdevs.wallhaven.domain.usecases

import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.domain.repository.MainRepository
import com.enigmaticdevs.wallhaven.util.DispatcherProvider
import com.enigmaticdevs.wallhaven.util.Resource
import javax.inject.Inject

class GetWallsBySortUseCase @Inject constructor(
    private val repository: MainRepository,
    private val dispatcher: DispatcherProvider,

) {

    suspend operator fun invoke(query : String,
                                 sorting : String,
                                 topRange : String,
                                 params: Params,
                                 page : Int) : Resource<Wallpapers?> {
        val result = repository.getSearchWallpapers(query,sorting,topRange,params,page)
        result?.let{
            return  Resource.Success(result)
        } ?: run{
            return Resource.Error("Failed")
        }
    }
}