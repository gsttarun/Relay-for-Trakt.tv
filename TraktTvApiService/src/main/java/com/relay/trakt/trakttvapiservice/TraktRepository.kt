package com.relay.trakt.trakttvapiservice

import android.content.*
import android.net.Uri
import android.net.UrlQuerySanitizer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.CLIENT_ID
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.GRANT_TYPE
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.REDIRECT_URI
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.RESPONSE_TYPE
import com.relay.trakt.trakttvapiservice.constants.ApiConstants.RESPONSE_TYPE_CODE
import com.relay.trakt.trakttvapiservice.constants.Constants
import com.relay.trakt.trakttvapiservice.constants.Constants.Preferences.TRAKT_PREFERENCES
import com.relay.trakt.trakttvapiservice.constants.TraktStatusCodes
import com.relay.trakt.trakttvapiservice.model.standardMedia.Movie
import com.relay.trakt.trakttvapiservice.model.userSettings.UserSettings
import com.relay.trakt.trakttvapiservice.request.AccessTokenRequest
import com.relay.trakt.trakttvapiservice.request.RefreshTokenRequest
import com.relay.trakt.trakttvapiservice.request.RevokeAccessRequest
import timber.log.Timber
import java.lang.ref.WeakReference


object TraktRepository {

    private val TAG = this::class.java.simpleName

    private lateinit var clientId: String
    private lateinit var clientSecret: String
    private lateinit var redirectURI: String
    private var isStaging: Boolean = false
    private var accessToken: String? = null
    private var refreshToken: String? = null


    internal lateinit var weakContext: WeakReference<Context>
    private lateinit var mReceiver: BroadcastReceiver
    private lateinit var onAuthorizedLiveData: MutableLiveData<Resource<String>>
    private var sharedPreferences: SharedPreferences? = null


    private lateinit var apiService: ApiService

    private fun initBroadCastReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.hasExtra(Constants.Intent.AUTH_CODE)) {
                    val code = intent.getSerializableExtra(Constants.Intent.AUTH_CODE) as String
                    getAccessTokenFromServer(code)
                } else if (intent.hasExtra(Constants.Intent.ERROR)) {
                    val error = intent.getSerializableExtra(Constants.Intent.ERROR) as String
                    val errorDescription = intent.getSerializableExtra(Constants.Intent.ERROR_DESCRIPTION) as String
                    onAuthorizedLiveData.postValue(Resource.error(error, data = errorDescription))
                }
                weakContext.doWithContext {
                    it.unregisterReceiver(mReceiver)
                }
            }
        }
    }


    fun initialize(
            clientId: String,
            clientSecret: String,
            redirectURI: String,
            weakContext: WeakReference<Context>,
            isStaging: Boolean = true,
            enableLogging: Boolean = false
    ) {
        this.clientId = clientId
        this.clientSecret = clientSecret
        this.redirectURI = redirectURI
        this.weakContext = weakContext
        this.isStaging = isStaging
        this.weakContext.doWithContext {
            this.sharedPreferences = it.getSharedPreferences(TRAKT_PREFERENCES, Context.MODE_PRIVATE)
            apiService =
                    getApiService(
                            baseUrl = if (isStaging) Constants.STAGING_URL else Constants.BASE_URL,
                            clientId = clientId,
                            context = it)
        }
        accessToken = sharedPreferences?.getString(Constants.Preferences.DATA_ACCESS_TOKEN, null)
        refreshToken = sharedPreferences?.getString(Constants.Preferences.DATA_REFRESH_TOKEN, null)

        if (enableLogging && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())
    }


    fun isAuthorized(): Boolean = accessToken != null
    fun isNotAuthorized(): Boolean = accessToken == null

    private fun getAuthorizeUrl(): String {
        val queryMap = getAuthorizeQueryMap()
        val url = if (isStaging) Constants.STAGING_AUTH_URL else Constants.AUTH_URL
        return apiService.authorize(url, queryMap).request().url.toString()
    }

    fun authorizeInApp(): LiveData<Resource<String>> {
        onAuthorizedLiveData = MutableLiveData()

        if (isNotAuthorized()) {
            weakContext.doWithContext {
                onAuthorizedLiveData.postValue(Resource.loading())
                initBroadCastReceiver()
                val intentFilter = IntentFilter(Constants.IntentAction.AUTH_CODE)
                it.registerReceiver(mReceiver, intentFilter)
                val url = getAuthorizeUrl()

                val intent = Intent(it, OauthActivity::class.java).apply {
                    putExtra(Constants.Intent.AUTH_URL, url)
                    putExtra(Constants.Intent.REDIRECT_URI, redirectURI)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                it.startActivity(intent)

                Timber.i(url)
            }
        }

        return onAuthorizedLiveData
    }

    /**
     * Call this method from onNewIntent in the activity which handles link redirection
     */
    fun handleResultFromBrowser(intent: Intent? = null): LiveData<Resource<String>> {
        onAuthorizedLiveData = MutableLiveData()
        if (isNotAuthorized()) {
            getCode(
                    intent,
                    onCodeFound = { code ->
                        if (code.isNotEmpty()) {
                            getAccessTokenFromServer(code)
                        } else {
                            authorizeFromExternalBrowser()
                        }
                    },
                    onError = { errorValue, errorDescription ->
                        Timber.e("error = $errorValue")
                        Timber.e("error description = $errorDescription")
                        onAuthorizedLiveData.postValue(Resource.error(errorValue, null, errorDescription))
                    }
            )
        }
        return onAuthorizedLiveData
    }

    /**
     * Call this when you want to trigger authorization from outside app
     * using browser then to handle the data
     */
    fun authorizeFromExternalBrowser() {
        val url = getAuthorizeUrl()
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        weakContext.doWithContext {
            it.startActivity(browserIntent)
        }
    }

    private fun getCode(
            intent: Intent?,
            onCodeFound: (String) -> Unit,
            onError: (errorValue: String, errorDescription: String) -> Unit
    ) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.dataString?.let { url ->
                if (url.startsWith("$redirectURI")) {
                    val sanitizer = UrlQuerySanitizer(url)
                    if (url.contains(Constants.CODE)) {
                        onCodeFound(sanitizer.getValue(Constants.CODE))
                        /*url.split("=")[1]?.let {
                            onCodeFound(it)
                        }*/
                    } else if (url.contains(Constants.ERROR)) {
                        val errorValue = sanitizer.getValue(Constants.ERROR)
                        val errorDescription = sanitizer.getValue(Constants.ERROR_DESCRIPTION)
                        onError(errorValue, errorDescription)
                    }
                } else {
                    onError("INVALID REDIRECT URI", "The redirect URI doesn't match the URI with which the TraktRepository was initialized.")
                }
            }
        } else {
            onError("Intent does not have ACTION_VIEW action", "This intent isn't for ")
        }
    }

    private fun getAuthorizeQueryMap(): HashMap<String, String> {
        return hashMapOf(
                RESPONSE_TYPE to RESPONSE_TYPE_CODE,
                CLIENT_ID to clientId,
                REDIRECT_URI to redirectURI
        )
    }

    fun getAccessTokenFromServer(code: String) {

        val bodyJson = AccessTokenRequest(
                code = code,
                grantType = GRANT_TYPE.AUTHORIZATION_CODE,
                clientSecret = clientSecret,
                redirectUri = redirectURI,
                clientId = clientId
        )

        onAuthorizedLiveData.postValue(Resource.loading())
        apiService.getAccessToken(bodyJson).enqueue(rCallback {
            onSuccess { authTokenResponse, _ ->
                authTokenResponse?.accessToken?.let { accessToken ->
                    this@TraktRepository.accessToken = accessToken
                    sharedPreferences?.putString(Constants.Preferences.DATA_ACCESS_TOKEN, accessToken)
                }

                authTokenResponse?.refreshToken?.let { refreshToken ->
                    this@TraktRepository.refreshToken = refreshToken
                    sharedPreferences?.putString(Constants.Preferences.DATA_REFRESH_TOKEN, refreshToken)
                }

//                if (isStaging) apiService = getApiService(Constants.STAGING_URL, clientId)

                onAuthorizedLiveData.postValue(Resource.success(null))
            }
            onFailure { authTokenResponse, message, _, rawResponse ->
                if (rawResponse != null) {
                    if (rawResponse.code() == TraktStatusCodes.C401.code) {
                        onAuthorizedLiveData.postValue(Resource.error("Invalid Client, Please remove app from recents and open app again"))
                    }
                } else {
                    authTokenResponse?.errorDescription?.let {
                        onAuthorizedLiveData.postValue(Resource.error(it))
                    }
                }
            }
        })
    }

    fun refreshAccessTokenFromServer() {
        val bodyJson = RefreshTokenRequest(
                refreshToken = refreshToken,
                grantType = GRANT_TYPE.REFRESH_TOKEN,
                clientSecret = clientSecret,
                redirectUri = redirectURI,
                clientId = clientId
        )

        apiService.refreshAccessToken(bodyJson).enqueue(rCallback {
            onSuccess { authTokenResponse, message ->
                authTokenResponse?.accessToken?.let { accessToken ->
                    this@TraktRepository.accessToken = accessToken
                    sharedPreferences?.putString(Constants.Preferences.DATA_ACCESS_TOKEN, accessToken)
                }

                authTokenResponse?.refreshToken?.let { refreshToken ->
                    this@TraktRepository.refreshToken = refreshToken
                    sharedPreferences?.putString(Constants.Preferences.DATA_REFRESH_TOKEN, refreshToken)
                }

//                if (isStaging) apiService = getApiService(Constants.STAGING_URL, clientId)
            }
            // TODO: handle failure case
        })
    }

    fun revokeAccessToken(): LiveData<Resource<Void>> {
        val revokeAccessResultLiveData = MutableLiveData<Resource<Void>>()

        val bodyJson = RevokeAccessRequest(
                token = accessToken,
                clientSecret = clientSecret,
                clientId = clientId
        )

        revokeAccessResultLiveData.postValue(Resource.loading())
        apiService.revokeAccessToken(bodyJson).enqueue(rCallback {
            onSuccess { _, message ->
                revokeAccessResultLiveData.postValue(Resource.success(null, message))
            }
            onFailure { _, message, throwable, _ ->
                message?.let {
                    revokeAccessResultLiveData.postValue(Resource.error(message, throwable, null))
                }
            }
        })

        return revokeAccessResultLiveData
    }

    fun clearData() {
        accessToken = null
        refreshToken = null
        sharedPreferences?.edit()?.clear()?.apply()
    }


    fun getAccessToken(): String? {
        return accessToken
    }

    fun getRefreshToken(): String? {
        return accessToken
    }


    suspend fun getUserSettings(): UserSettings = apiService.getUserSettings()

    suspend fun getPopularMovies(): List<Movie> = apiService.getPopularMovies()

}