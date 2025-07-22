// Top-level build file where you can add configuration options common to all sub-projects/modules.
// This version contains the corrected plugin alias syntax.

@Suppress("DSL_SCOPE_VIOLATION") // Suppress false positive warning for 'libs'
plugins {
    alias(libs.plugins.android.application) version "8.11.1" apply false
    alias(libs.plugins.android.library) apply false

    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.openapi.generator) apply false
}

// Global project properties
extra["ndkVersion"] = "27.0.12077973"
extra["cmakeVersion"] = "3.22.1"
extra["compileSdkVersion"] = 36
extra["targetSdkVersion"] = 36
extra["minSdkVersion"] = 33
extra["kotlinVersion"] = libs.versions.kotlin.get()

// Configure all projects for consistency
allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjvm-target=24",
                "-opt-in=kotlin.RequiresOptIn",
                "-Xcontext-receivers",
                "-Xjvm-default=all",
                "-Xskip-prerelease-check",
                "-Xexplicit-api=strict"
            )
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "24"
        targetCompatibility = "24"
        options.encoding = "UTF-8"
        options.isIncremental = true
        options.compilerArgs.add("--enable-preview")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("--enable-preview")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}


// Oracle Drive specific dependencies
dependencies {
    // Testing dependencies for Oracle Drive
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    
    // Dagger Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
