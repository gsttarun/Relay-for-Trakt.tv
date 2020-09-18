package com.relay.trakt.trakttvapiservice.model.userSettings

import com.google.gson.annotations.SerializedName

data class SharingText(

	@field:SerializedName("rated")
	val rated: String? = null,

	@field:SerializedName("watched")
	val watched: String? = null,

	@field:SerializedName("watching")
	val watching: String? = null
)