plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
}

// Added to specify Java version for this subproject
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    // Core Kotlin libraries
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Networking (without Android dependencies)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp3.logging.interceptor)

    // Utilities (JVM compatible only)
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.commons.compress)
    implementation(libs.xz)

    // Security (JVM compatible only)
    implementation(libs.bouncycastle)

    // Testing (JVM only)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.engine)
}

// ===== AUTOMATED QUALITY CHECKS =====
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "indent_size" to "4",
                    "max_line_length" to "120",
                ),
            )
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// ===== AUTOMATED TESTING =====
kover {
    reports {
        total {
            html {
                onCheck = true
            }
            xml {
                onCheck = true
            }
        }
    }
}

// ===== DOCUMENTATION =====
tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Genesis Core Module")
            moduleVersion.set("1.0.0")
            includes.from("Module.md")

            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }

            perPackageOption {
                matchingRegex.set(".*\\.internal.*")
                suppress.set(true)
            }
        }
    }
}

// ===== BUILD AUTOMATION TASKS =====
tasks.register("checkCodeQuality") {
    group = "verification"
    description = "Run all code quality checks for core module"
    dependsOn("spotlessCheck")
}

tasks.register("runAllTests") {
    group = "verification"
    description = "Run all tests with coverage for core module"
    dependsOn("test", "koverHtmlReport")
}

tasks.register("buildAndTest") {
    group = "build"
    description = "Complete build and test cycle for core module"
    dependsOn("checkCodeQuality", "build", "runAllTests")
}

// Note: API generation handled by app module
// Core module provides shared utilities only
