plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    compileOnly("javax.inject:javax.inject:1")

    testImplementation(libs.bundles.testing.unit)
}
