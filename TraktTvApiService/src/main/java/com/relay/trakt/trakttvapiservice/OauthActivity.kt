package com.relay.trakt.trakttvapiservice

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.UrlQuerySanitizer
import android.os.Build
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.relay.trakt.trakttvapiservice.Constants.CODE
import kotlinx.android.synthetic.main.activity_oauth.*
import timber.log.Timber


class OauthActivity : AppCompatActivity() {

    private val webViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url.toString()
            return if (url.startsWith("$redirectUri")) {
                val sanitizer = UrlQuerySanitizer(url)
                if (url.contains(CODE)) {
                    url.split("=")[1]?.let {
                        val intent = Intent(Constants.IntentAction.AUTH_CODE).apply {
                            putExtra(Constants.Intent.AUTH_CODE, it)
                        }
                        sendBroadcast(intent)
                    }
                } else if (url.contains(Constants.ERROR)) {
                    val errorValue = sanitizer.getValue(Constants.ERROR)
                    val errorDescription = sanitizer.getValue(Constants.ERROR_DESCRIPTION)
                    val intent = Intent(Constants.IntentAction.AUTH_CODE).apply {
                        putExtra(Constants.Intent.ERROR, errorValue)
                        putExtra(Constants.Intent.ERROR_DESCRIPTION, errorDescription)
                    }
                    sendBroadcast(intent)
                    Timber.e("error = $errorValue")
                    Timber.e("error description = $errorDescription")
                }
                finish()
                true
            } else
                super.shouldOverrideUrlLoading(view, request)
        }

    }

    private lateinit var redirectUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)

        intent?.getStringExtra(Constants.Intent.AUTH_URL)?.let {
            redirectUri = intent?.getStringExtra(Constants.Intent.REDIRECT_URI).toString()
            webview.apply {
                clearHistory()
                clearFormData()
                clearMatches()
                clearCache(true)
            }

            clearCookies(this)

            webview.webViewClient = webViewClient

            webview.loadUrl(it)
            Timber.e(it)
        }
    }

    fun clearCookies(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } else if (context != null) {
            val cookieSyncManager = CookieSyncManager.createInstance(context)
            cookieSyncManager.startSync()
            val cookieManager: CookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncManager.stopSync()
            cookieSyncManager.sync()
        }
    }
}
