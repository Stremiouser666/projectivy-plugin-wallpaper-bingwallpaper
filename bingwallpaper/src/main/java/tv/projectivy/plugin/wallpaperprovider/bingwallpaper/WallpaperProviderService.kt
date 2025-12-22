package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperDisplayMode
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType

class WallpaperProviderService : Service() {

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private val binder = object : IWallpaperProviderService.Stub() {

        override fun getWallpapers(): List<Wallpaper> {

            // 🔴 REPLACE THIS URL WITH YOUR M3U8 STREAM
            val hlsUrl = "https://example.com/stream/playlist.m3u8"

            Log.d("BingWallpaper", "Sending HLS wallpaper: $hlsUrl")

            return listOf(
                Wallpaper(
                    id = "hls-wallpaper-1",
                    title = "Live HLS Wallpaper",
                    url = hlsUrl,
                    type = WallpaperType.VIDEO,   // MUST be VIDEO
                    displayMode = WallpaperDisplayMode.FIT
                )
            )
        }

        override fun getEvents(): List<Event> = emptyList()

        override fun getPreferences(): String {
            return PreferencesManager.export()
        }

        override fun setPreferences(params: String) {
            PreferencesManager.import(params)
        }
    }
}
