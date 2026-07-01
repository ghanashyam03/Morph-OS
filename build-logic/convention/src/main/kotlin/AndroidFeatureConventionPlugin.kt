import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("morphos.android.library")
                apply("morphos.hilt")
                apply("morphos.compose")
            }

            dependencies.run {
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:common"))
                add("testImplementation", project(":core:testing"))

                val libs = target.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
                add("implementation", libs.findLibrary("lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
            }
        }
    }
}
