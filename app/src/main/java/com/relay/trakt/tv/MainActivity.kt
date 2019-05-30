package com.relay.trakt.tv

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.relay.trakt.trakttvapiservice.TraktRepository

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TraktRepository.initialize("https://api-staging.trakt.tv","","")

        TraktRepository.authorize()
    }
}
