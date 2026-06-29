import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }
            extensions.configure(JavaPluginExtension::class.java) {
                toolchain.languageVersion.set(JavaLanguageVersion.of(17))
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
        }
    }
}
