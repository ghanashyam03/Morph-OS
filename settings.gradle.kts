pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")

rootProject.name = "MorphOS"

include(":app")
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:ai")
include(":core:widget")
include(":core:testing")
include(":llama-runtime")
project(":llama-runtime").projectDir = file("core/ai/src/main/cpp/llama.cpp/examples/llama.android/lib")
include(":feature:dashboard")
include(":feature:widget-creator")
include(":feature:settings")
include(":feature:onboarding")
