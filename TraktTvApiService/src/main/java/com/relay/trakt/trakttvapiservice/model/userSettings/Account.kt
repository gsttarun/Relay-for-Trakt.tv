package com.relay.trakt.trakttvapiservice.model.userSettings

import com.google.gson.annotations.SerializedName

data class Account(

	@field:SerializedName("timezone")
	val timezone: String? = null,

	@field:SerializedName("date_format")
	val dateFormat: String? = null,

	@field:SerializedName("cover_image")
	val coverImage: String? = null,

	@field:SerializedName("time_24hr")
	val time24hr: Boolean? = null
)