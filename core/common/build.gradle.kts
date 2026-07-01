plugins {
    id("morphos.android.library")
    id("morphos.hilt")
    id("morphos.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.morphos.app.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    
    testImplementation(libs.bundles.testing.unit)
}
