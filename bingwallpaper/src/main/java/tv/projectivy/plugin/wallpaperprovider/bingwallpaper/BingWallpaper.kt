package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import android.content.Context
import android.view.Surface
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import tv.projectivy.launcher.wallpaper.WallpaperProvider

class BingWallpaperProvider {

    private var exoPlayer: ExoPlayer? = null

    fun playWallpaper(context: Context, wallpaper: BingWallpaper, surface: Surface) {
        if (wallpaper.isStream) {
            // Play HLS stream using ExoPlayer
            exoPlayer?.release() // release previous player if exists
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(wallpaper.mediaUri)
                setMediaItem(mediaItem)
                setVideoSurface(surface)
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                play()
            }
        } else {
            // TODO: existing image/video logic
            // e.g., load image with Glide or play MP4 using default player
        }
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
