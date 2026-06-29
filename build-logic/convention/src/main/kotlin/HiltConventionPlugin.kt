import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            dependencies.add("implementation", libs.findLibrary("hilt.android").get())
            dependencies.add("ksp", libs.findLibrary("hilt.compiler").get())
        }
    }
}
