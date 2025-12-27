package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "M3U8Settings"
        private const val TEST_TIMEOUT_MS = 10_000L
    }

    private var testPlayer: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var testCompleted = false
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            PreferencesManager.init(this)
        } catch (e: Exception) {
            Log.e(TAG, "PreferencesManager init failed", e)
        }
        
        setContentView(R.layout.activity_settings)

        val urlInput = findViewById<EditText>(R.id.m3u8_url_input)
        val testButton = findViewById<Button>(R.id.test_button)
        val saveButton = findViewById<Button>(R.id.save_button)

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
                showDebugDialog("Starting Rutube extraction...")
                extractRutubeM3U8(url) { result ->
                    when {
                        result.success && result.m3u8Url != null -> {
                            showDebugDialog("✅ Extracted M3U8!\n\nURL: ${result.m3u8Url.take(100)}...")
                            urlInput.setText(result.m3u8Url)
                            testStream(result.m3u8Url)
                        }
                        else -> {
                            showDebugDialog("❌ Extraction Failed\n\n${result.errorMessage}")
                        }
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
                showDebugDialog("Extracting and saving...")
                extractRutubeM3U8(url) { result ->
                    when {
                        result.success && result.m3u8Url != null -> {
                            saveUrl(result.m3u8Url)
                        }
                        else -> {