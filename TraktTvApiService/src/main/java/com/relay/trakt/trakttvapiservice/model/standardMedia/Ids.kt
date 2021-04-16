package com.relay.trakt.trakttvapiservice.model.standardMedia

import com.google.gson.annotations.SerializedName

data class Ids(

	@field:SerializedName("tmdb")
	val tmdb: Int? = null,

	@field:SerializedName("imdb")
	val imdb: String? = null,

	@field:SerializedName("tvdb")
	val tvdb: Int? = null,

	@field:SerializedName("trakt")
	val trakt: Int? = null,

	@field:SerializedName("slug")
	val slug: String? = null
)