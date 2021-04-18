package com.relay.trakt.tv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.relay.trakt.trakttvapiservice.Resource
import com.relay.trakt.trakttvapiservice.TraktRepository
import com.relay.trakt.trakttvapiservice.getNotNullMessage
import com.relay.trakt.trakttvapiservice.model.standardMedia.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception

class TestViewModel : ViewModel() {

    fun getPopularMovies(): LiveData<Resource<List<Movie>>> {
        val mutaBLeLiveData = MutableLiveData<Resource<List<Movie>>>()

        viewModelScope.launch {
            Timber.e("lauched")
            withContext(Dispatchers.IO) {
                try {
                    val popularMovies = TraktRepository.getPopularMovies()
                    mutaBLeLiveData.postValue(Resource.success(popularMovies))
                } catch (e: Exception) {
                    mutaBLeLiveData.postValue(Resource.error(e.getNotNullMessage(), e))
                }
            }
        }

        Timber.e("returned")
        return mutaBLeLiveData
    }
}