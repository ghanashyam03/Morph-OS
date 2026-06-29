plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    compileOnly("com.google.dagger:hilt-core:${libs.versions.hilt.asProvider().get()}")
    compileOnly("javax.inject:javax.inject:1")
}
