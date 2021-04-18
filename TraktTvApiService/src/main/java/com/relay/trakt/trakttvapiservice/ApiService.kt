package com.relay.trakt.trakttvapiservice

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.JsonObject
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.APPLICATION_JSON
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.AUTHORIZATION
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.CONTENT_TYPE
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.TRAKT_API_KEY
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.TRAKT_API_VERSION
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.TRAKT_API_VERSION_NUMBER
import com.relay.trakt.trakttvapiservice.model.authToken.AuthTokenResponse
import com.relay.trakt.trakttvapiservice.model.movies.TrendingMovies
import com.relay.trakt.trakttvapiservice.model.standardMedia.Movie
import com.relay.trakt.trakttvapiservice.model.userSettings.UserSettings
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

    @POST("/oauth/revoke")
    fun revokeAccessToken(@Body bodyJson: RevokeAccessRequest): Call<JsonObject>

    @GET("/users/settings")
    suspend fun getUserSettings(): UserSettings

    @GET("/movies/trending") // this API has pagination
    suspend fun getTrendingMovies(@QueryMap queryMap: Map<String, Int>): List<TrendingMovies>

    @GET("/movies/popular") // this API has pagination
    suspend fun getPopularMovies(): List<Movie>
}

fun getApiService(baseUrl: String, clientId: String, enableHttpLogging: Boolean = true, context:Context): ApiService {

    val headerInterceptor = Interceptor {
        val request = it.request().let { request ->
            request.newBuilder().apply {
                addHeader(CONTENT_TYPE, APPLICATION_JSON)
                addHeader(TRAKT_API_KEY, clientId)
                addHeader(TRAKT_API_VERSION, TRAKT_API_VERSION_NUMBER)
                TraktRepository.getAccessToken()?.let { accessToken ->
                    addHeader(AUTHORIZATION, "BEARER $accessToken")
                }
            }
        }.build()

        return@Interceptor it.proceed(request)
    }

    val interceptor = ChuckerInterceptor.Builder(context)
            .alwaysReadResponseBody(true)
            .redactHeaders(TRAKT_API_KEY, AUTHORIZATION)
            .build()

    val okHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(60, TimeUnit.SECONDS)
        addInterceptor(headerInterceptor)
        addInterceptor(interceptor)
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
    return HttpLoggingInterceptor { message ->
        Timber.i(message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}