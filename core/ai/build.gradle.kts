plugins {
    id("morphos.android.library")
    id("morphos.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.morphos.app.core.ai"
    
    buildFeatures {
        buildConfig = true
    }
    
    defaultConfig {
        buildConfigField("String", "OPENROUTER_API_KEY", "\"dummy_key\"")
    }
}

dependencies {
    implementation(project(":llama-runtime"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // ONNX Runtime
    implementation(libs.onnxruntime.android)

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
