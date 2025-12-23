plugins {
    id("com.android.library") version "8.12.2" apply false
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "tv.projectivy.plugin.wallpaperprovider.bingwallpaper"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    // Projectivy API dependency (if used)
}
