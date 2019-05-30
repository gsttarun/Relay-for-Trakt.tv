package com.relay.trakt.trakttvapiservice

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url
import timber.log.Timber
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET
    fun authorize(
        @Url url: String,
        @QueryMap queryMap: Map<String, String>
    ): Call<JSONObject>
}

fun getApiService(baseUrl: String, clientId: String): ApiService {

    val httpLoggingInterceptor =
        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
            Timber.i(message)
//            Log.i("ApiService", message)
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    val headerInterceptor = Interceptor {
        val request = it.request()?.let { request ->
            request.newBuilder()?.apply {
                addHeader(CONTENT_TYPE, APPLICATION_JSON)
                addHeader(TRAKT_API_KEY, clientId)
                addHeader(TRAKT_API_VERSION, "2")
            }
        }?.build()

        return@Interceptor it.proceed(request)
    }

    val okHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(60, TimeUnit.SECONDS)
        addInterceptor(headerInterceptor)
        addInterceptor(httpLoggingInterceptor)
//        authenticator(TokenAuthenticator())
    }.build()

    return Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}