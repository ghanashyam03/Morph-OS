plugins {
    id("morphos.android.feature")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.morphos.app.feature.settings"
}

dependencies {
    implementation(libs.accompanist.permissions)
}
