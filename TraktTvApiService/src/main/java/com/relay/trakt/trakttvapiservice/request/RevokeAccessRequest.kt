package com.relay.trakt.trakttvapiservice.request

import com.google.gson.annotations.SerializedName

data class RevokeAccessRequest(

	@field:SerializedName("client_secret")
	val clientSecret: String? = null,

	@field:SerializedName("client_id")
	val clientId: String? = null,

	@field:SerializedName("token")
	val token: String? = null
)