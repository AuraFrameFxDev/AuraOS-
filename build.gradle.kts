// Minimal build.gradle.kts for AuraOS - Without Android dependencies for now
plugins {
    kotlin("jvm") version "1.8.10" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}