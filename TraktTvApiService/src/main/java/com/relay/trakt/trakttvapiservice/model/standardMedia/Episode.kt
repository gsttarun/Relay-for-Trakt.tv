package com.relay.trakt.trakttvapiservice.model.standardMedia

import com.google.gson.annotations.SerializedName

data class Episode(

		@field:SerializedName("number")
	val number: Int? = null,

		@field:SerializedName("season")
	val season: Int? = null,

		@field:SerializedName("ids")
	val ids: Ids? = null,

		@field:SerializedName("title")
	val title: String? = null
)