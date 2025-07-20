pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    // Version catalogs are automatically detected from gradle/libs.versions.toml
}



rootProject.name = "AuraFrameFX"
include(":app")