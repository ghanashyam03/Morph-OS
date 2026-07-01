plugins {
    id("morphos.android.feature")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.morphos.app.feature.widgetcreator"
}

dependencies {
    implementation(project(":core:widget"))
    implementation(libs.hilt.navigation.compose)
}
