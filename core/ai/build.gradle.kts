plugins {
    id("morphos.android.library")
    id("morphos.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.morphos.app.core.ai"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    // ONNX Runtime
    implementation(libs.onnxruntime.android)

    // Llama.cpp local AAR
    implementation(files("libs/llama-android.aar"))

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coroutines & Serialization
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Timber
    implementation(libs.timber)

    testImplementation(project(":core:testing"))
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)
}
