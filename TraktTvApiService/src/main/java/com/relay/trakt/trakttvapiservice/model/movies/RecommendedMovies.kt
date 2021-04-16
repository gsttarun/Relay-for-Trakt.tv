package com.relay.trakt.trakttvapiservice.model.movies

import com.google.gson.annotations.SerializedName
import com.relay.trakt.trakttvapiservice.model.standardMedia.Movie

data class RecommendedMovies(

	@field:SerializedName("RecommendedMovies")
	val recommendedMovies: List<RecommendedMoviesItem?>? = null
)

data class RecommendedMoviesItem(

	@field:SerializedName("user_count")
	val userCount: Int? = null,

	@field:SerializedName("movie")
	val movie: Movie? = null
)
