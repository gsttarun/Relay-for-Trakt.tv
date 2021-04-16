package com.relay.trakt.trakttvapiservice.model.standardMedia

import com.google.gson.annotations.SerializedName

data class Person(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("ids")
	val ids: Ids? = null
)