package com.relay.trakt.trakttvapiservice.request

import com.google.gson.annotations.SerializedName

data class AccessTokenRequest(

	@field:SerializedName("code")
	val code: String? = null,

	@field:SerializedName("grant_type")
	val grantType: String? = null,

	@field:SerializedName("client_secret")
	val clientSecret: String? = null,

	@field:SerializedName("redirect_uri")
	val redirectUri: String? = null,

	@field:SerializedName("client_id")
	val clientId: String? = null
)