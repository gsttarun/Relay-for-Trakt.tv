package com.relay.trakt.trakttvapiservice.constants

internal object Constants {
    object IntentAction {
        const val AUTH_CODE = "android.intent.action.AUTH_CODE"
    }

    object Intent {
        const val REDIRECT_URI = "redirect_uri"
        const val AUTH_URL = "authUrl"
        const val ERROR_DESCRIPTION = "error_description"
        const val ERROR = "error"
        const val AUTH_CODE = "auth_code"
    }

    object Preferences {
        const val TRAKT_PREFERENCES = "trakt_preferences"
        const val DATA_ACCESS_TOKEN = "access_token"
        const val DATA_REFRESH_TOKEN = "refresh_token"
    }

    const val AUTH_URL = "https://trakt.tv/oauth/authorize"
    const val STAGING_AUTH_URL = "https://staging.trakt.tv/oauth/authorize"
    const val BASE_URL = "https://api.trakt.tv"
    const val STAGING_URL = "https://api-staging.trakt.tv"
    const val ERROR_DESCRIPTION = "error_description"
    const val ERROR = "error"
    const val CODE = "code"
}