package com.relay.trakt.trakttvapiservice

object ApiConstants {

    const val RESPONSE_TYPE = "response_type"
    const val RESPONSE_TYPE_CODE = "code"
    const val CLIENT_ID = "client_id"
    const val CLIENT_SECRET = "client_secret"
    const val REDIRECT_URI = "redirect_uri"
    const val STATE = "state"
    const val AUTHORIZATION = "Authorization"
    const val REFRESH_TOKEN = "refresh_token"

    const val CONTENT_TYPE = "Content-type"
    const val APPLICATION_JSON = "application/json"
    const val TRAKT_API_KEY = "trakt-api-key"
    const val TRAKT_API_VERSION = "trakt-api-version"
    const val BEARER = "Bearer"
    const val PAGE = "page"
    const val LIMIT = "limit"

    object GRANT_TYPE {
        const val GRANT_TYPE = "grant_type"
        const val REFRESH_TOKEN = "refresh_token"
        const val AUTHORIZATION_CODE = "authorization_code"

    }

}