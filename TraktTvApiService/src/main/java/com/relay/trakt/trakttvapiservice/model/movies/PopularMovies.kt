package com.relay.trakt.trakttvapiservice.model.movies

import com.google.gson.annotations.SerializedName
import com.relay.trakt.trakttvapiservice.model.Ids

data class PopularMovies(

		@field:SerializedName("year")
	val year: Int? = null,

		@field:SerializedName("ids")
	val ids: Ids? = null,

		@field:SerializedName("title")
	val title: String? = null
)