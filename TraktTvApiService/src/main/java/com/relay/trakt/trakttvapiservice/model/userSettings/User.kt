package com.relay.trakt.trakttvapiservice.model.userSettings

import com.google.gson.annotations.SerializedName

data class User(

		@field:SerializedName("vip_years")
	val vipYears: Int? = null,

		@field:SerializedName("private")
	val jsonMemberPrivate: Boolean? = null,

		@field:SerializedName("images")
	val images: Images? = null,

		@field:SerializedName("gender")
	val gender: String? = null,

		@field:SerializedName("about")
	val about: String? = null,

		@field:SerializedName("vip_og")
	val vipOg: Boolean? = null,

		@field:SerializedName("joined_at")
	val joinedAt: String? = null,

		@field:SerializedName("name")
	val name: String? = null,

		@field:SerializedName("ids")
	val ids: UserSettingIds? = null,

		@field:SerializedName("vip_ep")
	val vipEp: Boolean? = null,

		@field:SerializedName("location")
	val location: String? = null,

		@field:SerializedName("vip")
	val vip: Boolean? = null,

		@field:SerializedName("age")
	val age: Int? = null,

		@field:SerializedName("username")
	val username: String? = null
)