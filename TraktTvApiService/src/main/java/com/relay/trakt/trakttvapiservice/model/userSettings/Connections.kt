package com.relay.trakt.trakttvapiservice.model.userSettings

import com.google.gson.annotations.SerializedName

data class Connections(

	@field:SerializedName("twitter")
	val twitter: Boolean? = null,

	@field:SerializedName("facebook")
	val facebook: Boolean? = null,

	@field:SerializedName("slack")
	val slack: Boolean? = null,

	@field:SerializedName("tumblr")
	val tumblr: Boolean? = null,

	@field:SerializedName("google")
	val google: Boolean? = null,

	@field:SerializedName("medium")
	val medium: Boolean? = null
)