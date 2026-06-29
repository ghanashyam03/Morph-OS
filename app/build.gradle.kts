plugins {
    id("morphos.android.application")
    id("morphos.hilt")
    id("morphos.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
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

    implementation(libs.navigation.compose)
    implementation(libs.timber)
    
    debugImplementation(libs.leakcanary)
}
