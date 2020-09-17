package com.relay.trakt.trakttvapiservice.model.userSettings

import com.google.gson.annotations.SerializedName

data class Avatar(

	@field:SerializedName("full")
	val full: String? = null
)