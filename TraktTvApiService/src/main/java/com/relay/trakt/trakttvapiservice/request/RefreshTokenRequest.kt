package com.relay.trakt.trakttvapiservice.request

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(

	@field:SerializedName("refresh_token")
	val refreshToken: String? = null,

	@field:SerializedName("grant_type")
	val grantType: String? = null,

	@field:SerializedName("client_secret")
	val clientSecret: String? = null,

	@field:SerializedName("redirect_uri")
	val redirectUri: String? = null,

	@field:SerializedName("client_id")
	val clientId: String? = null
)