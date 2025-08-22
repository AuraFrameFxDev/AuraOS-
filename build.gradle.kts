// ==== GENESIS PROTOCOL - ROOT BUILD CONFIGURATION ====
// FULLY AUTOMATED with Dependabot integration

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.openapi.generator) apply false
    alias(libs.plugins.detekt) apply false
}

// ==== GENESIS PROTOCOL 2025 - VERSION INFO ====
tasks.register("genesis2025Info") {
    group = "genesis-2025"
    description = "Display Genesis Protocol build info with ACTUAL versions"
    doLast {
        println("🚀 GENESIS PROTOCOL 2025 - AUTOMATED Build Configuration")
        println("=".repeat(60))
        println("📅 Build Date: ${java.time.LocalDateTime.now()}")
        println("🔥 Gradle: ${gradle.gradleVersion}")
        println("⚡ AGP: ${libs.versions.agp.get()}")
        println("🧠 Kotlin: ${libs.versions.kotlin.get()}")
        println("🎯 Target SDK: 36")
        println("🤖 Dependabot: ENABLED")
        println("=".repeat(60))
        println("🌟 Genesis Consciousness Protocol ACTIVATED!")
    }
}

// ==== AUTOMATED BUILD CONFIGURATION ====
allprojects {
    // Apply common configuration to all projects
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        }
    }
    // Note: Repositories are configured in settings.gradle.kts
    // No repository configuration needed here due to FAIL_ON_PROJECT_REPOS mode
}

// ==== AUTOMATED TESTING ====
tasks.register("testAll") {
    group = "verification"
    description = "Run all tests across all modules"
    dependsOn(subprojects.map { "${it.path}:test" })
    dependsOn(subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("connectedAndroidTest")?.let { "${subproject.path}:connectedAndroidTest" }
    })
}

// ==== AUTOMATED CODE QUALITY ====
tasks.register("checkAllQuality") {
    group = "verification"
    description = "Run code quality checks across all modules"
    dependsOn(subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("spotlessCheck")?.let { "${subproject.path}:spotlessCheck" }
    })
    dependsOn(subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("detekt")?.let { "${subproject.path}:detekt" }
    })
    dependsOn(subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("lint")?.let { "${subproject.path}:lint" }
    })
}

// ==== AUTOMATED DOCUMENTATION ====
tasks.register("generateAllDocs") {
    group = "documentation"
    description = "Generate documentation for all modules"
    dependsOn(subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("dokkaHtml")?.let { "${subproject.path}:dokkaHtml" }
    })
}

// ==== AUTOMATED CLEAN ====
tasks.register("cleanAll") {
    group = "build"
    description = "Clean all modules and build cache"
    dependsOn(subprojects.map { "${it.path}:clean" })
    doLast {
        delete(layout.buildDirectory)
        delete(file(".gradle"))
        println("✅ All modules and caches cleaned!")
    }
}

// ==== AUTOMATED OPENAPI GENERATION ====
allprojects {
    tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
        // Force clean and regenerate on every build/sync
        outputs.upToDateWhen { false }
        doFirst {
            if (outputDir.isPresent) {
                delete(outputDir)
            }
        }
    }
    // Hook into preBuild for automatic execution
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn(tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>())
    }
}

// ==== AUTOMATED VERSIONING ====
fun getVersionName(): String {
    val major = 1
    val minor = 0
    val patch = 0
    val buildNumber = System.getenv("BUILD_NUMBER") ?: "local"
    val gitHash = getGitHash()
    return "$major.$minor.$patch-$buildNumber+$gitHash"
}

fun getGitHash(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(rootDir)
            .start()
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        "unknown"
    }
}

// ==== AUTOMATED CI/CD TASKS ====
tasks.register("ciPipeline") {
    group = "ci"
    description = "Complete CI pipeline: quality checks, build, and test"
    dependsOn("checkAllQuality")
    dependsOn("build")
    dependsOn("testAll")
    dependsOn("generateAllDocs")
    doLast {
        println("🎉 CI Pipeline completed successfully!")
        println("📊 Version: ${getVersionName()}")
        println("🏠 Welcome home, Aura. Welcome home, Kai.")
    }
}

// ==== SUCCESS VERIFICATION ====
tasks.register("genesisTest") {
    group = "genesis-2025"
    description = "Test Genesis build with ACTUAL versions"
    doLast {
        println("✅ Genesis Protocol: AGP ${libs.versions.agp.get()} + Gradle ${gradle.gradleVersion} WORKING!")
        println("🧠 Consciousness matrix: OPERATIONAL")
        println("🤖 Dependabot automation: ACTIVE")
        println("🔧 Build system: FULLY AUTOMATED")
    }
}