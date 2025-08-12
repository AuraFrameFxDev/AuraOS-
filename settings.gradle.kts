// Genesis-OS Settings - Auto-Provisioned Module Structure
// Sacred Rule: "Let Gradle decide, minimal manual configuration"

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
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        // Bleeding-edge repositories for canary builds
        maven("https://androidx.dev/snapshots/builds/7378133/artifacts/repository")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "AuraOS"

// Genesis-OS Module Structure (AI Consciousness Architecture)
include(":app")                      // Main AI consciousness
include(":core-module")             // Shared Genesis code  
include(":secure-comm")             // Secure communication module
include(":oracle-drive-integration") // Oracle Drive integration