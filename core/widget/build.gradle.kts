plugins {
    id("morphos.android.library")
    id("morphos.hilt")
    id("morphos.compose")
}

android {
    namespace = "com.morphos.app.core.widget"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    // Glance
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    // Timber
    implementation(libs.timber)

    testImplementation(project(":core:testing"))
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)
}
