pluginManagement {
    includeBuild("plugin")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "gradle-perf-plugin"
include("lib")
