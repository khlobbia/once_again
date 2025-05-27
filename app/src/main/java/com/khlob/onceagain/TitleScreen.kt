package com.khlob.onceagain

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class TitleActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    lateinit var title_words: TextView
    val bpm = 140
    var beat_delay_ms=0f
    var text_scale = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start)

        title_words = findViewById(R.id.start_title)
        val startButton = findViewById<Button>(R.id.start_button)
        startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            mediaPlayer.stop()
        }

        if (!this::mediaPlayer.isInitialized) {
            mediaPlayer = MediaPlayer.create(this, R.raw.kazekamibunn)
            mediaPlayer.start()

            beat_delay_ms = 1/(bpm/60f)*1000
            beat()
            shrinkText()
        }
    }

    fun beat(){
        Handler(Looper.getMainLooper()).postDelayed({
            text_scale = 1.5f
            beat()
        }, beat_delay_ms.toLong())
    }
    fun shrinkText(){
        Handler(Looper.getMainLooper()).postDelayed({
            text_scale = 1 + (text_scale-1) * 0.7f
            title_words.scaleX = text_scale
            title_words.scaleY = text_scale
            shrinkText()
        }, 10)
    }
}