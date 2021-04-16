package com.relay.trakt.tv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.relay.trakt.trakttvapiservice.Status
import com.relay.trakt.trakttvapiservice.TraktRepository
import com.relay.trakt.trakttvapiservice.rObserver
import com.relay.trakt.tv.databinding.ActivityMainBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val onAuthorizedObserver = rObserver<String> {
        onLoading {
            binding.progressIndicator.visible()
        }
        onSuccess { _, _ ->
            binding.textv.text = TraktRepository.getAccessToken().toString()
            hideAuthButtons()
            binding.progressIndicator.gone()

        }
        onError { message, throwable ->
            binding.textv.text = message
            binding.progressIndicator.gone()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (TraktRepository.isAuthorized()) {
            binding.textv.text = TraktRepository.getAccessToken()
            hideAuthButtons()
        } else showAuthButtons()

        binding.authInAppButton.onClick {
            if (TraktRepository.isNotAuthorized())
                TraktRepository.authorizeInApp().observe(this, onAuthorizedObserver)
        }

        binding.authInBrowserButton.onClick {
            if (TraktRepository.isNotAuthorized())
                TraktRepository.authorizeFromExternalBrowser()
        }

        binding.logoutButton.onClick {
            TraktRepository.revokeAccessToken().observe(this, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.progressIndicator.visible()
                    }
                    Status.SUCCESS -> {
                        Snackbar.make(binding.textv, "Access Revoked", Snackbar.LENGTH_SHORT).show()
                        TraktRepository.clearData()
                        showAuthButtons()
                        binding.logoutButton.gone()
                        binding.progressIndicator.gone()
                        binding.textv.text = "Logout Successful"
                    }
                    Status.ERROR -> {
                        TraktRepository.clearData()
                        Snackbar.make(binding.textv, "Logout Failed", Snackbar.LENGTH_SHORT).show()
                        binding.progressIndicator.gone()
                    }
                }
            })
        }
    }

    private fun hideAuthButtons() {
        withAll(binding.authInAppButton, binding.authInBrowserButton, binding.orTextLabel) {
            invisible()
        }
        binding.logoutButton.visible()
    }

    private fun showAuthButtons() {
        withAll(binding.authInAppButton, binding.authInBrowserButton, binding.orTextLabel) {
            visible()
        }
        binding.logoutButton.gone()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == Intent.ACTION_VIEW) {
            if (TraktRepository.isNotAuthorized()) {
                TraktRepository.handleResultFromBrowser(intent).observe(this, onAuthorizedObserver)
            } else {
                binding.textv.text = TraktRepository.getAccessToken().toString()
            }
        }
    }
}
