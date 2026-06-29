import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.Test

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure(ApplicationExtension::class.java) {
                compileSdk = 35

                buildFeatures {
                    buildConfig = true
                }

                defaultConfig {
                    applicationId = "com.morphos.app"
                    minSdk = 26
                    targetSdk = 35
                    versionCode = 1
                    versionName = "1.0"
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }

            tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = "17"
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-opt-in=kotlin.RequiresOptIn",
                        "-Xcontext-receivers"
                    )
                }
            }

            tasks.withType(Test::class.java).configureEach {
                useJUnitPlatform()
            }
        }
    }
}
