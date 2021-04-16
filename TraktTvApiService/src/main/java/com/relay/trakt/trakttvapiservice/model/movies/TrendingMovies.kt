package com.relay.trakt.trakttvapiservice.model.movies

import com.google.gson.annotations.SerializedName
import com.relay.trakt.trakttvapiservice.model.standardMedia.Movie

data class TrendingMovies(

        @field:SerializedName("movie")
        val movie: Movie? = null,

        @field:SerializedName("watchers")
        val watchers: Int? = null
)