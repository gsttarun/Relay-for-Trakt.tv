package com.relay.trakt.tv

import android.app.Application
import com.relay.trakt.trakttvapiservice.TraktRepository
import com.singhajit.sherlock.core.Sherlock
import timber.log.Timber
import java.lang.ref.WeakReference

class RelayApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Sherlock.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        TraktRepository.initialize(
                clientId = BuildConfig.CLIENT_ID,
                clientSecret = BuildConfig.CLIENT_SECRET,
                redirectURI = BuildConfig.REDIRECT_URI,
                weakContext = WeakReference(this@RelayApplication),
                isStaging = BuildConfig.IS_STAGING
        )
    }
}