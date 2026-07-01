plugins {
    id("morphos.android.library")
    id("morphos.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.morphos.app.core.domain"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    
    testImplementation(libs.bundles.testing.unit)
}
