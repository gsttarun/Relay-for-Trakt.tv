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

class TestActivity : AppCompatActivity() {

    val onAuthorizedObserver = Observer<Resource<String>> {
        when (it.status) {
            Status.LOADING -> {
                progressIndicator.visible()
            }
            Status.SUCCESS -> {
                textv.text = TraktRepository.getAccessToken().toString()
                hideAuthButtons()
                progressIndicator.gone()
            }
            Status.ERROR -> {
                textv.text = it.message
                progressIndicator.gone()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (TraktRepository.isAuthorized()) {
            textv.text = TraktRepository.getAccessToken()
            hideAuthButtons()
        } else showAuthButtons()

        authInAppButton.onClick {
            if (TraktRepository.isNotAuthorized())
                TraktRepository.authorizeInApp().observe(this, onAuthorizedObserver)
        }

        authInBrowserButton.onClick {
            if (TraktRepository.isNotAuthorized())
                TraktRepository.authorizeFromExternalBrowser()
        }

        logoutButton.onClick {
            TraktRepository.revokeAccessToken().observe(this, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        progressIndicator.visible()
                    }
                    Status.SUCCESS -> {
                        Snackbar.make(textv, "Access Revoked", Snackbar.LENGTH_SHORT).show()
                        TraktRepository.clearData()
                        showAuthButtons()
                        logoutButton.gone()
                        progressIndicator.gone()
                        textv.text = "Logout Successful"
                    }
                    Status.ERROR -> {
                        TraktRepository.clearData()
                        Snackbar.make(textv, "Logout Failed", Snackbar.LENGTH_SHORT).show()
                        progressIndicator.gone()
                    }
                }
            })
        }
    }

    private fun hideAuthButtons() {
        withAll(authInAppButton, authInBrowserButton, orTextLabel) {
            invisible()
        }
        logoutButton.visible()
    }

    private fun showAuthButtons() {
        withAll(authInAppButton, authInBrowserButton, orTextLabel) {
            visible()
        }
        logoutButton.gone()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_VIEW) {
            if (TraktRepository.isNotAuthorized()) {
                TraktRepository.handleResultFromBrowser(intent).observe(this, onAuthorizedObserver)
            } else {
                textv.text = TraktRepository.getAccessToken().toString()
            }
        }
    }
}
