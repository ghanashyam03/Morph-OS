plugins {
    id("morphos.android.library")
    id("morphos.hilt")
}

android {
    namespace = "com.morphos.app.core.testing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    // Expose testing libraries to consumer modules
    implementation(libs.junit5.api)
    implementation(libs.junit5.params)
    implementation(libs.mockk)
    implementation(libs.mockk.android)
    implementation(libs.turbine)
    implementation(libs.robolectric)
    implementation(libs.espresso.core)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.room.testing)
    implementation(libs.work.testing)
}
