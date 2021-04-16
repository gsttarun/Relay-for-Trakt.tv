package com.relay.trakt.trakttvapiservice.model.standardMedia

import com.google.gson.annotations.SerializedName

data class Season(

	@field:SerializedName("number")
	val number: Int? = null,

	@field:SerializedName("ids")
	val ids: Ids? = null
)