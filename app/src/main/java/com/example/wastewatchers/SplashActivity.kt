package com.example.wastewatchers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashVideo: VideoView = findViewById(R.id.splashVideo)
        val videoUri = Uri.parse("android.resource://$packageName/${R.raw.intro}")
        splashVideo.setVideoURI(videoUri)

        splashVideo.setOnCompletionListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        splashVideo.start()
    }
}
