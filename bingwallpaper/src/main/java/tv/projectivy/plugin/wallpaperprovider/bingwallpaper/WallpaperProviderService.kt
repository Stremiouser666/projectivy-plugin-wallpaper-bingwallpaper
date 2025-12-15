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

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private val binder = object : IWallpaperProviderService.Stub() {

        override fun getWallpapers(event: Event?): List<Wallpaper> {
            val m3u8Url = PreferencesManager.wallpaperSourceUrl

            if (m3u8Url.isNullOrBlank()) {
                Log.w("WallpaperProvider", "No M3U8 URL configured")
                return emptyList()
            }

            Log.d("WallpaperProvider", "Providing M3U8 wallpaper: $m3u8Url")

            return listOf(
                Wallpaper(
                    m3u8Url,
                    WallpaperType.VIDEO,
                    WallpaperDisplayMode.DEFAULT,
                    "Live Wallpaper",
                    m3u8Url,
                    "M3U8 Stream"
                )
            )
        }

        override fun getPreferences(): String {
            return PreferencesManager.export()
        }

        override fun setPreferences(params: String) {
            PreferencesManager.import(params)
        }
    }
}

