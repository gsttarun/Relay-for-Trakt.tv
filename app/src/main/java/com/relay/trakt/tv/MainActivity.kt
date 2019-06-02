package com.relay.trakt.tv

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.relay.trakt.trakttvapiservice.TraktRepository
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val onAuthorizedObserver = Observer<Boolean> {
        when (it) {
            true -> {
                textv.text = TraktRepository.accessToken.toString()
            }
            false -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (TraktRepository.isNotAuthorized()) {
            TraktRepository.authorizeInApp().observe(this, onAuthorizedObserver)
        } else {
            textv.text = TraktRepository.accessToken.toString()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (TraktRepository.isNotAuthorized()) {
            TraktRepository.authorizeFromBrowser(intent).observe(this, onAuthorizedObserver)
        } else {
            textv.text = TraktRepository.accessToken.toString()
        }
    }
}
