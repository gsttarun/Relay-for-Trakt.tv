package com.relay.trakt.trakttvapiservice.model

import com.google.gson.annotations.SerializedName

data class Person(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("ids")
	val ids: Ids? = null
)