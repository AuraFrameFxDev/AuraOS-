import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// This file configures the buildSrc module, which contains custom build logic.
// It has its own isolated classpath and cannot access the root project's version catalog (`libs`).
// Therefore, its dependencies and versions are defined directly here.

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

// Define the specific versions required for the buildSrc module itself.
// These are aligned with our main project's toolchain.
val kotlinVersion = "2.2.0"
val agpVersion = "8.13.0"

// Configure Kotlin compilation for the buildSrc module
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        // It's good practice to align the language and API version
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        
        // CORRECTED: The previous `jvmTarget.set(...)` method was not recognized
        // by the isolated buildSrc compiler. Using freeCompilerArgs is a more direct
        // and robust way to set the JVM target.
        freeCompilerArgs.add("-Xjvm-target=24")
    }
}

// Configure Java compilation for the buildSrc module
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "24"
    targetCompatibility = "24"
}

dependencies {
    // We must declare the dependencies for buildSrc explicitly, without using the `libs` catalog.
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$agpVersion")
    // Add any other dependencies that buildSrc itself might need here.

    // Test dependencies for buildSrc
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

// Configure the test runner for buildSrc tests
tasks.withType<Test> {
    useJUnitPlatform()
}

