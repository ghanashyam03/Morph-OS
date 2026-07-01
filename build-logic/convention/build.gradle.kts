plugins {
    `kotlin-dsl`
}

group = "com.morphos.app.buildlogic"

dependencies {
    compileOnly("com.android.tools.build:gradle:8.7.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    compileOnly("com.google.dagger:hilt-android-gradle-plugin:2.52")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.21-1.0.28")
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "morphos.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "morphos.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "morphos.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("kotlinJvm") {
            id = "morphos.kotlin.jvm"
            implementationClass = "KotlinJvmConventionPlugin"
        }
        register("hilt") {
            id = "morphos.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("compose") {
            id = "morphos.compose"
            implementationClass = "ComposeConventionPlugin"
        }
    }
}
