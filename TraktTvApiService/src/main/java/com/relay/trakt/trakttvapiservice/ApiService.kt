package com.relay.trakt.trakttvapiservice

import com.relay.trakt.trakttvapiservice.ApiConstants.APPLICATION_JSON
import com.relay.trakt.trakttvapiservice.ApiConstants.AUTHORIZATION
import com.relay.trakt.trakttvapiservice.ApiConstants.CONTENT_TYPE
import com.relay.trakt.trakttvapiservice.ApiConstants.TRAKT_API_KEY
import com.relay.trakt.trakttvapiservice.ApiConstants.TRAKT_API_VERSION
import com.relay.trakt.trakttvapiservice.model.authToken.AuthTokenResponse
import com.relay.trakt.trakttvapiservice.request.AccessTokenRequest
import com.relay.trakt.trakttvapiservice.request.RefreshTokenRequest
import com.relay.trakt.trakttvapiservice.request.RevokeAccessRequest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET
    fun authorize(
        @Url url: String,
        @QueryMap queryMap: Map<String, String>
    ): Call<JSONObject>

    @POST("/oauth/token")
    fun getAccessToken(@Body bodyJson: AccessTokenRequest): Call<AuthTokenResponse>

    @POST("/oauth/token")
    fun refreshAccessToken(@Body bodyJson: RefreshTokenRequest): Call<AuthTokenResponse>

    @POST("/oauth/token")
    fun revokeAccessToken(@Body bodyJson: RevokeAccessRequest): Call<AuthTokenResponse>
}

fun getApiService(baseUrl: String, clientId: String, enableHttpLogging: Boolean = true): ApiService {

    val headerInterceptor = Interceptor {
        val request = it.request()?.let { request ->
            request.newBuilder()?.apply {
                addHeader(CONTENT_TYPE, APPLICATION_JSON)
                addHeader(TRAKT_API_KEY, clientId)
                addHeader(TRAKT_API_VERSION, "2")
                TraktRepository.accessToken?.let { accessToken ->
                    addHeader(AUTHORIZATION, "BEARER $accessToken")
                }
            }
        }?.build()

        return@Interceptor it.proceed(request)
    }

    val okHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(60, TimeUnit.SECONDS)
        addInterceptor(headerInterceptor)
        if (enableHttpLogging) addInterceptor(getHttpLoggingInterceptor())
//        authenticator(TokenAuthenticator())
    }.build()

    return Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(baseUrl)
//        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create()
}

private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
        Timber.i(message)
    }).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}