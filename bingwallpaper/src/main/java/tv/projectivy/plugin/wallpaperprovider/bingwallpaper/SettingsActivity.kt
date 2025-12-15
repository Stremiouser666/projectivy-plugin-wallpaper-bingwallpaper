package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        PreferencesManager.init(this)

        val urlInput = findViewById<EditText>(R.id.m3u8_url_input)
        val saveButton = findViewById<Button>(R.id.save_button)

        // Load existing value
        urlInput.setText(PreferencesManager.wallpaperSourceUrl ?: "")

        saveButton.setOnClickListener {
            val url = urlInput.text.toString().trim()

            if (!isValidM3U8Url(url)) {
                Toast.makeText(
                    this,
                    "Please enter a valid M3U8 (.m3u8) URL",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            PreferencesManager.wallpaperSourceUrl = url

            Toast.makeText(
                this,
                "M3U8 wallpaper source saved",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    private fun isValidM3U8Url(url: String): Boolean {
        if (url.isBlank()) return false
        if (!Patterns.WEB_URL.matcher(url).matches()) return false
        return url.endsWith(".m3u8", ignoreCase = true)
    }
}

