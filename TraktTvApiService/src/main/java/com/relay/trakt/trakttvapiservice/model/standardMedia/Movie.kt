package com.relay.trakt.trakttvapiservice.model.standardMedia

import com.google.gson.annotations.SerializedName

data class Movie(

		@field:SerializedName("year")
	val year: Int? = null,

		@field:SerializedName("ids")
	val ids: Ids? = null,

		@field:SerializedName("title")
	val title: String? = null
)