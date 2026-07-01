plugins {
    id("morphos.android.feature")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.morphos.app.feature.onboarding"
}

dependencies {
    implementation(project(":core:ai"))
    implementation(libs.accompanist.permissions)
}
