package com.relay.trakt.tv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.relay.trakt.trakttvapiservice.Resource
import com.relay.trakt.trakttvapiservice.Status
import com.relay.trakt.trakttvapiservice.TraktRepository
import com.relay.trakt.trakttvapiservice.model.standardMedia.Movie
import com.relay.trakt.trakttvapiservice.rObserver
import com.relay.trakt.tv.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class TestActivity : AppCompatActivity(), CoroutineScope {
    protected lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


    private lateinit var binding: ActivityMainBinding

    private lateinit var testViewModel: TestViewModel

    val rObserver = rObserver<List<Movie>> {
        onSuccess { data: List<Movie>?, message: String? ->
            Timber.e("Popular Movies List Downloaded")
            Snackbar.make(binding.textv, "Popular Movies List Downloaded", Snackbar.LENGTH_SHORT).show()
        }
        onError { message, _ ->
            Snackbar.make(binding.textv, message.toString(), Snackbar.LENGTH_SHORT).show()
        }
    }
    private val onAuthorizedObserver = rObserver<String> {
        onLoading {
            binding.progressIndicator.visible()
        }
        onSuccess { _, _ ->
            binding.textv.text = TraktRepository.getAccessToken().toString()
            hideAuthButtons()
            binding.progressIndicator.gone()

            binding.getPopularMoviesButton.visible()
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

        job = Job()

        testViewModel = ViewModelProviders.of(this).get(TestViewModel::class.java)

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
                        binding.getPopularMoviesButton.gone()
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

        binding.getPopularMoviesButton.onClick{
            testViewModel.getPopularMovies().observe(this@TestActivity, rObserver)
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
