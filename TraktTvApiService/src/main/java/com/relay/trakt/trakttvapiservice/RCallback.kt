package com.relay.trakt.trakttvapiservice

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@DslMarker
annotation class RCallbackDsl

@RCallbackDsl
class RCallback<T> {
    private var success: (T?, message: String?) -> Unit = { _, _ -> }
    private var failure: (T?, message: String?, throwable: Throwable?,response: Response<T>?) -> Unit = { _, _, _ ,_-> }

    fun onSuccess(init: (T?, message: String?) -> Unit) {
        success = init
    }

    fun onFailure(init: (T?, message: String?, throwable: Throwable?,response: Response<T>?) -> Unit) {
        failure = init
    }

    companion object {
        fun <T> getCallback(init: RCallback<T>.() -> Unit): Callback<T> {
            val rCallback = RCallback<T>().apply(init)
            return object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    rCallback.failure(null, t.message!!, t,null)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        rCallback.success(response.body(), response.message())
                    } else {
                        rCallback.failure(response.body(), response.message(), null,response)
                    }
                }
            }
        }
    }
}