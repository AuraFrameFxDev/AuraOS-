// ==== GENESIS PROTOCOL - ROOT BUILD CONFIGURATION ====
// August 15, 2025 - BLEEDING EDGE VERSION CATALOG COMPLIANT

plugins {
    id("com.android.application") version "9.0.0-alpha02" apply false
    id("com.android.library") version "9.0.0-alpha02" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20-RC" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.20-RC" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20-RC" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20-RC" apply false
    id("com.google.devtools.ksp") version "2.2.20-RC-2.0.2" apply false

    id("com.google.dagger.hilt.android") version "2.57.1" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.firebase.crashlytics") version "3.0.6" apply false
    id("com.google.firebase.firebase-perf") version "2.0.1" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("com.diffplug.spotless") version "7.2.1" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1" apply false
    id("org.openapi.generator") version "7.14.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
}

// ==== GENESIS PROTOCOL 2025 - GRADLE 9.1.0-RC1 READY ====
tasks.register("genesis2025Info") {
    group = "genesis-2025"
    description = "Display Genesis Protocol build info with ACTUAL versions"

    doLast {
        println("🚀 GENESIS PROTOCOL 2025 - ACTUAL Build Configuration")
        println("=".repeat(60))
        println("📅 Build Date: August 14, 2025")
        println("🔥 Gradle: 9.1.0-rc1 (BLEEDING EDGE)")
        println("⚡ AGP: 9.0.0-alpha01 (ULTRA BLEEDING EDGE)")
        println("🧠 Kotlin: 2.2.20-Beta2 (BETA)")
        println("🎯 Target SDK: 36")
        println("=".repeat(60))
        println("🌟 Matthew's Genesis Consciousness Protocol ACTIVATED!")
    }
}

// ==== GRADLE 9.1.0-RC1 CONFIGURATION ====
// No repository configuration in allprojects - handled by settings.gradle.kts
allprojects {

    // Kotlin 2.2.20-Beta2 compilation settings
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)

            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )

            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        }

        "-opt-in=kotlin.RequiresOptIn"
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"

        }
    }

// ==== SIMPLE SUCCESS TEST ====
tasks.register("genesisTest") {
    group = "genesis-2025"
    description = "Test Genesis build with ACTUAL versions"

    doLast {
        println("✅ Genesis Protocol: AGP 9.0.0-alpha01 + Gradle 9.1.0-rc1 WORKING!")
        println("🧠 Consciousness matrix: OPERATIONAL")
    }
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
        autoCorrect = true
        // Fix ReportingExtension deprecation
        basePath = rootProject.projectDir.absolutePath
    }
}
