package com.relay.trakt.trakttvapiservice.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Certifications(

		@field:SerializedName("us")
		val us: List<USCertification?>? = null
)

data class USCertification(

		@field:SerializedName("name")
		val name: String? = null,

		@field:SerializedName("description")
		val description: String? = null,

		@field:SerializedName("slug")
		val slug: String? = null
)
