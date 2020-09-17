package com.relay.trakt.trakttvapiservice.model.userSettings

import com.google.gson.annotations.SerializedName

data class Images(

	@field:SerializedName("avatar")
	val avatar: Avatar? = null
)