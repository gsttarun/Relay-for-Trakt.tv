package com.relay.trakt.tv

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.relay.trakt.trakttvapiservice.Resource
import com.relay.trakt.trakttvapiservice.Status
import com.relay.trakt.trakttvapiservice.TraktRepository
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val onAuthorizedObserver = Observer<Resource<String>> {
        when (it.status) {
            Status.LOADING -> {

            }
            Status.SUCCESS -> {
                textv.text = TraktRepository.getAccessToken().toString()
                withAll(authInAppButton,authInBrowserButton){
                    invisible()
                }
            }
            Status.ERROR -> {
                textv.text = it.message
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (TraktRepository.isAuthorized()) {
            textv.text = TraktRepository.getAccessToken()
        }
        authInAppButton.setOnClickListener {
            if (TraktRepository.isNotAuthorized())
                TraktRepository.authorizeInApp().observe(this, onAuthorizedObserver)
        }

        authInBrowserButton.setOnClickListener {
            if (TraktRepository.isNotAuthorized())
                TraktRepository.authorizeFromExternalBrowser()
        }

        logoutButton.setOnClickListener {
            TraktRepository.revokeAccessToken().observe(this, Observer {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        textv.text = "Logout Successful"
                        Snackbar.make(textv, "Access Revoked", Snackbar.LENGTH_SHORT).show()
                        TraktRepository.clearData()
                        withAll(authInAppButton,authInBrowserButton){
                            visible()
                        }
                    }
                    Status.ERROR -> {
                        TraktRepository.clearData()
                        Snackbar.make(textv, "Logout Failed", Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (TraktRepository.isNotAuthorized()) {
            TraktRepository.handleResultFromBrowser(intent).observe(this, onAuthorizedObserver)
        } else {
            textv.text = TraktRepository.getAccessToken().toString()
        }
    }
}
