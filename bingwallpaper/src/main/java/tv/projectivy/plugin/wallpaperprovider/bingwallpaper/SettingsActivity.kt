package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class SettingsActivity : AppCompatActivity() {

    private var testPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        PreferencesManager.init(this)

        val urlInput = findViewById<EditText>(R.id.m3u8_url_input)
        val saveButton = findViewById<Button>(R.id.save_button)
        val testButton = findViewById<Button>(R.id.test_button)

        urlInput.setText(PreferencesManager.wallpaperSourceUrl ?: "")

        testButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (!isValidM3U8Url(url)) {
                toast("Enter a valid M3U8 URL first")
                return@setOnClickListener
            }
            testStream(url)
        }

        saveButton.setOnClickListener {
            val url = urlInput.text.toString().trim()

            if (!isValidM3U8Url(url)) {
                toast("Please enter a valid M3U8 (.m3u8) URL")
                return@setOnClickListener
            }

            PreferencesManager.wallpaperSourceUrl = url
            toast("M3U8 wallpaper source saved")
            finish()
        }
    }

    private fun testStream(url: String) {
        releaseTestPlayer()
        toast("Testing stream…")

        testPlayer = ExoPlayer.Builder(this).build().apply {
            volume = 0f

            addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        toast("✅ Stream OK")
                        releaseTestPlayer()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    toast("❌ Stream failed")
                    releaseTestPlayer()
                }
            })

            setMediaItem(MediaItem.fromUri(url))
            prepare()
            play()
        }
    }

    private fun releaseTestPlayer() {
        testPlayer?.release()
        testPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseTestPlayer()
    }

    private fun isValidM3U8Url(url: String): Boolean {
        
