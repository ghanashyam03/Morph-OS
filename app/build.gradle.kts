plugins {
    id("morphos.android.application")
    id("morphos.hilt")
    id("morphos.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.morphos.app"
    
    defaultConfig {
        applicationId = "com.morphos.app"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ai"))
    implementation(project(":core:widget"))
    
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:widget-creator"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:onboarding"))

    implementation(libs.room.runtime)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.navigation.compose)
    implementation(libs.timber)
    implementation(libs.android.startup)
    
    debugImplementation(libs.leakcanary)
}
