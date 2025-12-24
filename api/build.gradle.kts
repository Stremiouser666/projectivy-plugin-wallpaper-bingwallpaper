plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "tv.projectivy.plugin.wallpaperprovider.api"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
    
    buildFeatures {
        aidl = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}