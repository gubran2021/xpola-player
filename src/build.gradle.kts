plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.shahid.iptv"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.shahid.iptv"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.leanback:leanback:1.2.0")
    // Keep every Media3 component on the same version as the FFmpeg extension.
    implementation("androidx.media3:media3-exoplayer:1.6.1")
    implementation("androidx.media3:media3-ui:1.6.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.6.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.6.1")

    // Community-built Media3 FFmpeg audio decoder (version matches Media3 1.6.1).
    // FFmpeg support is primarily for additional audio codecs; video codec support
    // still depends on Android/device decoders and the actual container/stream.
    implementation("org.jellyfin.media3:media3-ffmpeg-decoder:1.6.1+2")
}
