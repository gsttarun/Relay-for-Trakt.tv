package com.relay.trakt.trakttvapiservice

import androidx.lifecycle.Observer

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
data class Resource<out T>(
        val status: Status,
        val data: T?,
        val message: String? = null,
        val throwable: Throwable? = null
) {
    companion object {
        fun <T> success(data: T?, msg: String? = ""): Resource<T> {
            return Resource(Status.SUCCESS, data, msg)
        }

        fun <T> error(msg: String, throwable: Throwable? = null, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, msg, throwable)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data)
        }
    }
}

enum class Status {
    LOADING,
    SUCCESS,
    ERROR
}

@DslMarker
annotation class ResourceObserverDsl

@ResourceObserverDsl
class RObserver<T> {
    private var success: (data: T?, message: String?) -> Unit = { _, _ -> }
    private var failure: (message: String?, throwable: Throwable?) -> Unit = { _, _ -> }
    private var loading: () -> Unit = { }

    fun onSuccess(init: (data: T?, message: String?) -> Unit) {
        success = init
    }

    fun onError(init: (message: String?, throwable: Throwable?) -> Unit) {
        failure = init
    }

    fun onLoading(init: () -> Unit) {
        loading = init
    }

    fun build(): Observer<Resource<T>> {
        return Observer<Resource<T>> {
            when (it?.status) {
                Status.LOADING -> {
                    loading()
                }
                Status.SUCCESS -> {
                    success(it.data, it.message)
                }
                Status.ERROR -> {
                    failure(it.message, it.throwable)
                }
            }
        }

    }
}

fun <T> rObserver(init: RObserver<T>.() -> Unit): Observer<Resource<T>> = RObserver<T>().apply(init).build()