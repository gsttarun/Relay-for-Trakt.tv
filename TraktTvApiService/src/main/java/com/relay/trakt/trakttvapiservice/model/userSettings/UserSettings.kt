package com.relay.trakt.trakttvapiservice.model.userSettings

import com.google.gson.annotations.SerializedName

data class UserSettings(

	@field:SerializedName("sharing_text")
	val sharingText: SharingText? = null,

	@field:SerializedName("user")
	val user: User? = null,

	@field:SerializedName("account")
	val account: Account? = null,

	@field:SerializedName("connections")
	val connections: Connections? = null
)