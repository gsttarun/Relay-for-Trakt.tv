package com.relay.trakt.trakttvapiservice.model

import com.google.gson.annotations.SerializedName

data class Season(

	@field:SerializedName("number")
	val number: Int? = null,

	@field:SerializedName("ids")
	val ids: Ids? = null
)