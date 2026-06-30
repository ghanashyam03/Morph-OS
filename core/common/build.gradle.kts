plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    compileOnly("com.google.dagger:hilt-core:${libs.versions.hilt.asProvider().get()}")
    compileOnly("javax.inject:javax.inject:1")

    // For AnimationUtils CompositionLocal
    compileOnly("androidx.compose.runtime:runtime:1.7.5") // compileOnly to avoid leaks

    testImplementation(libs.bundles.testing.unit)
}
