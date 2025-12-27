package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val TEST_TIMEOUT_MS = 10_000L
    }

    private var testPlayer: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var testCompleted = false
    private val httpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            PreferencesManager.init(this)
        } catch (e: Exception) {
            // PreferencesManager might not have init method
        }
        
        setContentView(R.layout.activity_settings)

        val urlInput = findViewById<EditText>(R.id.m3u8_url_input)
        val testButton = findViewById<Button>(R.id.test_button)
        val saveButton = findViewById<Button>(R.id.save_button)

        // Load saved URL
        try {
            urlInput.setText(PreferencesManager.wallpaperSourceUrl ?: "")
        } catch (e: Exception) {
            toast("Error loading saved URL: ${e.message}")
        }

        testButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isBlank()) {
                toast("Enter a URL")
                return@setOnClickListener
            }
            
            if (isRutubeUrl(url)) {
                toast("Extracting M3U8 from Rutube...")
                extractRutubeM3U8(url) { m3u8Url ->
                    if (m3u8Url != null) {
                        toast("Found M3U8! Testing...")
                        testStream(m3u8Url)
                    } else {
                        toast("❌ Failed to extract M3U8 from Rutube")
                    }
                }
            } else if (isValidM3U8Url(url)) {
                testStream(url)
            } else {
                toast("Enter a valid Rutube or M3U8 URL")
            }
        }

        saveButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isBlank()) {
                toast("Enter a URL")
                return@setOnClickListener
            }

            if (isRutubeUrl(url)) {
                toast("Extracting and saving M3U8...")
                extractRutubeM3U8(url) { m3u8Url ->
                    if (m3u8Url != null) {
                        saveUrl(m3u8Url)
                    } else {
                        toast("❌ Failed to extract M3U8 from Rutube")
                    }
                }
            } else if (isValidM3U8Url(url)) {
                saveUrl(url)
            } else {
                toast("Enter a valid Rutube or M3U8 URL")
            }
        }
    }

    private fun isRutubeUrl(url: String): Boolean {
        return url.contains("rutube.ru", ignoreCase = true)
    }

    private fun extractRutubeM3U8(rutubeUrl: String, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Extract video ID from URL
                val videoId = extractVideoId(rutubeUrl)
                if (videoId == null) {
                    withContext(Dispatchers.Main) { callback(null) }
                    return@launch
                }

                // Fetch video info from Rutube API
                val apiUrl = "https://rutube.ru/api/play/options/$videoId/?no_404=true&referer=https%3A%2F%2Frutube.ru"
                val request = Request.Builder().url(apiUrl).build()
                
                val response = httpClient.newCall(request).execute()
                val json = response.body?.string()
                
                if (json != null) {
                    val jsonObject = JSONObject(json)
                    val m3u8Url = jsonObject.optString("video_balancer")
                        .ifEmpty { jsonObject.optJSONObject("video_balancer")?.optString("m3u8") }
                    
                    withContext(Dispatchers.Main) {
                        callback(if (m3u8Url.isNullOrEmpty()) null else m3u8Url)
                    }
                } else {
                    withContext(Dispatchers.Main) { callback(null) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { callback(null) }
            }
        }
    }

    private fun extractVideoId(url: String): String? {
        // Extract video ID from URLs like:
        // https://rutube.ru/video/2cc3a117b2de6ebadb9d2037b37deb05/
        val regex = "rutube\\.ru/video/([a-f0-9]+)".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(url)?.groupValues?.get(1)
    }

    private fun saveUrl(url: String) {
        try {
            PreferencesManager.wallpaperSourceUrl = url
            toast("✅ M3U8 wallpaper source saved")
            finish()
        } catch (e: Exception) {
            toast("Error saving: ${e.message}")
        }
    }

    private fun testStream(url: String) {
        releaseTestPlayer()
        testCompleted = false

        toast("Testing stream…")

        try {
            testPlayer = ExoPlayer.Builder(this).build().apply {
                volume = 0f

                addListener(object : Player.Listener {

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY && !testCompleted) {
                            testCompleted = true

                            val format: Format? = videoFormat
                            val resolution = if (format != null && format.width > 0 && format.height > 0) {
                                "${format.width}×${format.height}"
                            } else {
                                "Unknown resolution"
                            }

                            val codec = format?.sampleMimeType ?: "Unknown codec"

                            toast("✅ Stream OK\n$resolution\n$codec")

                            releaseTestPlayer()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        if (!testCompleted) {
                            testCompleted = true
                            toast("❌ Stream failed: ${error.message}")
                            releaseTestPlayer()
                        }
                    }
                })

                setMediaItem(MediaItem.fromUri(url))
                prepare()
                play()
            }

            timeoutRunnable = Runnable {
                if (!testCompleted) {
                    testCompleted = true
                    toast("❌ Stream timeout (10s)")
                    releaseTestPlayer()
                }
            }
            handler.postDelayed(timeoutRunnable!!, TEST_TIMEOUT_MS)
        } catch (e: Exception) {
            toast("❌ Error testing stream: ${e.message}")
            testCompleted = true
            releaseTestPlayer()
        }
    }

    private fun releaseTestPlayer() {
        timeoutRunnable?.let { handler.removeCallbacks(it) }
        timeoutRunnable = null

        testPlayer?.release()
        testPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseTestPlayer()
    }

    private fun isValidM3U8Url(url: String): Boolean {
        if (url.isBlank()) return false
        if (!Patterns.WEB_URL.matcher(url).matches()) return false
        return url.contains(".m3u8", ignoreCase = true)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}