package com.relay.trakt.trakttvapiservice

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