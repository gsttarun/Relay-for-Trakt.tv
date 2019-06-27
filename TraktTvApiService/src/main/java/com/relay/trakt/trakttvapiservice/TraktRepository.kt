package com.relay.trakt.trakttvapiservice

import android.content.*
import android.net.Uri
import android.net.UrlQuerySanitizer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relay.trakt.trakttvapiservice.ApiConstants.CLIENT_ID
import com.relay.trakt.trakttvapiservice.ApiConstants.GRANT_TYPE
import com.relay.trakt.trakttvapiservice.ApiConstants.REDIRECT_URI
import com.relay.trakt.trakttvapiservice.ApiConstants.RESPONSE_TYPE
import com.relay.trakt.trakttvapiservice.ApiConstants.RESPONSE_TYPE_CODE
import com.relay.trakt.trakttvapiservice.Constants.Preferences.TRAKT_PREFERENCES
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
    var accessToken: String? = null
    var refreshToken: String? = null


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
                    getAccessToken(code)
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
        }
        accessToken = sharedPreferences?.getString(Constants.Preferences.DATA_ACCESS_TOKEN, null)
        refreshToken = sharedPreferences?.getString(Constants.Preferences.DATA_REFRESH_TOKEN, null)
        apiService = getApiService(Constants.BASE_URL, clientId)

        if (enableLogging && Timber.treeCount() == 0)
            Timber.plant(Timber.DebugTree())
    }


    fun isAuthorized(): Boolean = accessToken != null
    fun isNotAuthorized(): Boolean = accessToken == null

    private fun getAuthorizeUrl(): String {
        val queryMap = getAuthorizeQueryMap()
        return apiService.authorize(Constants.AUTH_URL, queryMap).request().url().toString()
    }

    fun authorizeInApp(): LiveData<Resource<String>> {
        onAuthorizedLiveData = MutableLiveData()

        if (isNotAuthorized()) {
            weakContext.doWithContext {
                initBroadCastReceiver()
                val intentFilter = IntentFilter(Constants.IntentAction.AUTH_CODE)
                it.registerReceiver(mReceiver, intentFilter)
                val url = getAuthorizeUrl()

                val intent = Intent(it, OauthActivity::class.java).apply {
                    putExtra(Constants.Intent.AUTH_URL, url)
                    putExtra(Constants.Intent.REDIRECT_URI, redirectURI)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                it.startActivity(intent)

                Timber.i(url)
            }
        }

        return onAuthorizedLiveData
    }

    /**
     * Call this first when you want to trigger authorization from outside app
     * using browser then to handle the data,
     * Call this method from onNewIntent in the activity which handles link redirection
     */
    fun authorizeFromBrowser(intent: Intent? = null): LiveData<Resource<String>> {
        onAuthorizedLiveData = MutableLiveData()
        if (isNotAuthorized()) {
            getCode(
                intent,
                onCodeFound = { code ->
                    if (code.isNotEmpty()) {
                        getAccessToken(code)
                    } else {
                        val url = getAuthorizeUrl()
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        weakContext.doWithContext {
                            it.startActivity(browserIntent)
                        }
                    }
                },
                onError = { errorValue, errorDescription ->
                    // TODO: return value via live data to outside library to show message to user
                }
            )
        }
        return onAuthorizedLiveData
    }

    private fun getCode(
        intent: Intent?,
        onCodeFound: (String) -> Unit,
        onError: (errorValue: String, errorDescription: String) -> Unit
    ) {
        if (intent?.action == Intent.ACTION_VIEW) {
            intent.data?.query?.let { url ->
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
                        Timber.e("error = $errorValue")
                        Timber.e("error description = $errorDescription")
                    }
                }
            }
        } else {
            Timber.w("Intent does not have ACTION_VIEW")
        }
    }

    private fun getAuthorizeQueryMap(): HashMap<String, String> {
        return hashMapOf(
            RESPONSE_TYPE to RESPONSE_TYPE_CODE,
            CLIENT_ID to clientId,
            REDIRECT_URI to redirectURI
        )
    }

    fun getAccessToken(code: String) {

        val bodyJson = AccessTokenRequest(
            code = code,
            grantType = GRANT_TYPE.AUTHORIZATION_CODE,
            clientSecret = clientSecret,
            redirectUri = redirectURI,
            clientId = clientId
        )

        apiService.getAccessToken(bodyJson).enqueue(RCallback.getCallback {
            onSuccess { authTokenResponse, _ ->
                authTokenResponse?.accessToken?.let { accessToken ->
                    this@TraktRepository.accessToken = accessToken
                    sharedPreferences?.putString(Constants.Preferences.DATA_ACCESS_TOKEN, accessToken)
                }

                authTokenResponse?.refreshToken?.let { refreshToken ->
                    this@TraktRepository.refreshToken = refreshToken
                    sharedPreferences?.putString(Constants.Preferences.DATA_REFRESH_TOKEN, refreshToken)
                }

                if (isStaging) apiService = getApiService(Constants.STAGING_URL, clientId)

                onAuthorizedLiveData.postValue(Resource.success(null))
            }
            onFailure { authTokenResponse, message, _ ->
                authTokenResponse?.errorDescription?.let {
                    onAuthorizedLiveData.postValue(Resource.error(it))
                }
            }
        })
    }

    fun refreshAccessToken() {
        val bodyJson = RefreshTokenRequest(
            refreshToken = refreshToken,
            grantType = GRANT_TYPE.REFRESH_TOKEN,
            clientSecret = clientSecret,
            redirectUri = redirectURI,
            clientId = clientId
        )

        apiService.refreshAccessToken(bodyJson).enqueue(RCallback.getCallback {
            onSuccess { authTokenResponse, message ->
                authTokenResponse?.accessToken?.let { accessToken ->
                    this@TraktRepository.accessToken = accessToken
                    sharedPreferences?.putString(Constants.Preferences.DATA_ACCESS_TOKEN, accessToken)
                }

                authTokenResponse?.refreshToken?.let { refreshToken ->
                    this@TraktRepository.refreshToken = refreshToken
                    sharedPreferences?.putString(Constants.Preferences.DATA_REFRESH_TOKEN, refreshToken)
                }

                if (isStaging) apiService = getApiService(Constants.STAGING_URL, clientId)
            }
            // TODO: handle failure case
        })
    }

    fun revokeAccessToken() {
        val bodyJson = RevokeAccessRequest(
            token = accessToken,
            clientSecret = clientSecret,
            clientId = clientId
        )

        apiService.revokeAccessToken(bodyJson).enqueue(RCallback.getCallback {

        })
    }

}