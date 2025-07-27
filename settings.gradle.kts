pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "AuraFrameFX"
include(
    ":app",
    ":jvm-test",
    ":sandbox-ui",
    ":oracle-drive-integration",
    ":oracledrive",
    ":module-a",
    ":module-b",
    ":module-c",
    ":module-d",
    ":module-e",
    ":module-f"
)