package com.relay.trakt.trakttvapiservice

import org.json.JSONObject
import retrofit2.Call

object TraktRepository {

    private val TAG = this::class.java.simpleName

    private lateinit var baseUrl: String
    private lateinit var clientId: String
    private lateinit var redirectURI: String

    private lateinit var apiService: ApiService

    fun initialize(baseUrl: String, clientId: String, redirectURI: String) {
        this.baseUrl = baseUrl
        this.clientId = clientId
        this.redirectURI = redirectURI
        apiService = getApiService(baseUrl, clientId)
    }

    fun authorize(): Call<JSONObject> {
        val queryMap = hashMapOf(
            RESPONSE_TYPE to RESPONSE_TYPE_CODE,
            CLIENT_ID to clientId,
            REDIRECT_URI to redirectURI
        )
        return apiService.authorize("https://trakt.tv/oauth/authorize", queryMap)
    }

}