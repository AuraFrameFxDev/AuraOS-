@file:Suppress("UnstableApiUsage")

// ===== GENESIS AUTO-PROVISIONED SETTINGS =====
// Gradle 9.1.0-rc1 + AGP 9.0.0-alpha01
// NO manual version catalog configuration needed!

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    repositories {
        google()                    // FIRST - Google for Android plugins
        gradlePluginPortal()        // SECOND - Gradle official plugins  
        mavenCentral()              // THIRD - Maven Central
        maven("https://androidx.dev/storage/compose-compiler/repository/") {
            name = "AndroidXDev"
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
            name = "JetBrainsCompose"
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "SonatypeSnapshots"
        }
        maven("https://jitpack.io") {
            name = "JitPack"
        }
    }
}

plugins {
    // Auto-provision Java toolchains
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()                    // FIRST - Google for AndroidX dependencies
        mavenCentral()              // SECOND - Maven Central for most libs
        maven("https://androidx.dev/storage/compose-compiler/repository/") {
            name = "AndroidXDev"
        }
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
            name = "JetBrainsCompose"
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "SonatypeSnapshots"
        }
        maven("https://jitpack.io") {
            name = "JitPack"
        }
    }
    // ✅ NO VERSION CATALOG CONFIG - Auto-discovered from gradle/libs.versions.toml
}

rootProject.name = "Genesis-Os"

// Genesis Protocol - Auto-discovered modules
include(":app")
include(":core-module")
include(":feature-module")
include(":datavein-oracle-native")
include(":oracle-drive-integration")
include(":secure-comm")
include(":sandbox-ui")
include(":collab-canvas")
include(":colorblendr")
include(":romtools")
include(":module-a", ":module-b", ":module-c", ":module-d", ":module-e", ":module-f")
