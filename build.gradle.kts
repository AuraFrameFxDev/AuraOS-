// Fixed build.gradle.kts for AuraOS
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.12.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}