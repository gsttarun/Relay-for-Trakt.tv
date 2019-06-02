package com.relay.trakt.trakttvapiservice.model.authToken

import com.google.gson.annotations.SerializedName

data class AuthTokenResponse(

    @field:SerializedName("access_token")
    val accessToken: String? = null,

    @field:SerializedName("refresh_token")
    val refreshToken: String? = null,

    @field:SerializedName("scope")
    val scope: String? = null,

    @field:SerializedName("created_at")
    val createdAt: Int? = null,

    @field:SerializedName("token_type")
    val tokenType: String? = null,

    @field:SerializedName("expires_in")
    val expiresIn: Int? = null
)