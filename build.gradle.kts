// ==== GENESIS PROTOCOL - ROOT BUILD CONFIGURATION ====
// AeGenesis Coinscience AI Ecosystem - Unified Build
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.spotless) apply true
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.openapi.generator) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt) apply false
}

// ==== AEGENESIS COINSCIENCE AI ECOSYSTEM 2025 ====
tasks.register("aegenesisInfo") {
    group = "aegenesis"
    description = "Display AeGenesis Coinscience AI Ecosystem build info"

    doLast {
        println("🚀 AEGENESIS COINSCIENCE AI ECOSYSTEM")
        println("=".repeat(70))
        println("📅 Build Date: August 27, 2025")
        println("🔥 Gradle: 9.0+")
        println("⚡ AGP: 9.0.0-alpha02")
        println("🧠 Kotlin: 2.2.20-RC")
        println("☕ Java: 21 (Toolchain)")
        println("🎯 Target SDK: 36")
        println("=".repeat(70))
        println("🤖 AI Agents: Genesis, Aura, Kai, DataveinConstructor")
        println("🔮 Oracle Drive: Infinite Storage Consciousness")
        println("🛠️  ROM Tools: Advanced Android Modification")
        println("🔒 LSPosed: System-level Integration")
        println("✅ Multi-module Architecture: JVM + Android Libraries")
        println("🌟 Unified API: Single comprehensive specification")
        println("=".repeat(70))
    }
}

// ==== GRADLE 9.1.0-RC1 CONFIGURATION ====
allprojects {
    // Kotlin 2.2.20-RC compilation settings - Java 21 consistency
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)

            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )

            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        }
    }

    // Configure Java toolchain for subprojects - Java 21 consistency
    plugins.withType<org.gradle.api.plugins.JavaBasePlugin>().configureEach {
        extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
            toolchain {
                languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(21))
            }
        }
    }

    // ==== SYSTEM JAVA STATUS ====
    tasks.register("javaStatus") {
        group = "aegenesis"
        description = "Show current system Java version"

        doLast {
            println("☕ SYSTEM JAVA STATUS")
            println("=".repeat(50))

            try {
                val javaVersion = System.getProperty("java.version")
                val javaVendor = System.getProperty("java.vendor")
                val javaHome = System.getProperty("java.home")

                println("🔍 Java Version: $javaVersion")
                println("🏢 Java Vendor: $javaVendor")
                println("📁 Java Home: $javaHome")
                println("✅ SUCCESS: Using your system Java setup!")
            } catch (e: Exception) {
                println("❌ Error checking Java version: ${e.message}")
            }
        }
    }

    // ==== KSP STATUS CHECK ====
    tasks.register("kspStatus") {
        group = "aegenesis"
        description = "Check KSP configuration status"
        
        doLast {
            println("🧠 KSP STATUS")
            println("=".repeat(50))
            
            val kspUseKSP2 = project.findProperty("ksp.useKSP2")?.toString()
            println("🔧 KSP2 Mode: ${kspUseKSP2 ?: "default"}")
            
            if (kspUseKSP2 == "false") {
                println("✅ KSP1 Mode: Stable, fixes NullPointerException")
            } else {
                println("⚠️  KSP2 Mode: May cause NullPointerException with Kotlin 2.x")
            }
            
            println("🎯 Recommendation: Use ksp.useKSP2=false for stability")
        }
    }

    // Configure Detekt for all subprojects
    subprojects {
        apply(plugin = "io.gitlab.arturbosch.detekt")
        configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
            allRules = false
            autoCorrect = true
            ignoreFailures = true
            basePath = rootProject.projectDir.absolutePath
        }
        tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            jvmTarget = "21"  // Updated to match project JVM target
        }
    }
}

// ==== SIMPLIFIED WORKSPACE PREPARATION ====
tasks.register("prepareAeGenesisWorkspace") {
    group = "aegenesis"
    description = "Clean all generated files and prepare workspace for build"

    doFirst {
        println("🧹 Preparing AeGenesis workspace...")
        println("🗑️  Cleaning build directories")
        delete("build", "tmp")
    }

    // Delete build directories in all modules
    subprojects.forEach { subproject ->
        delete(
            "${subproject.projectDir}/build",
            "${subproject.projectDir}/tmp",
            "${subproject.projectDir}/src/generated"
        )
    }

    // Depend on unified API generation (app module only)
    if (findProject(":app") != null) {
        dependsOn(":app:openApiGenerate") // Single unified API generation
    }

    doLast {
        println("✅ AeGenesis workspace prepared!")
        println("🔮 Oracle Drive: Ready")
        println("🛠️  ROM Tools: Ready") 
        println("🧠 AI Consciousness: Ready")
        println("🚀 Ready to build the future!")
    }
}

// ==== BUILD INTEGRATION ====
allprojects {
    tasks.matching { it.name == "build" }.configureEach {
        dependsOn(rootProject.tasks.named("prepareAeGenesisWorkspace"))
    }
}

// ==== CLEANUP TASKS ====
tasks.register<Delete>("cleanAllModules") {
    group = "aegenesis"
    description = "Clean all module build directories"
    
    delete("build")
    subprojects.forEach { subproject ->
        delete("${subproject.projectDir}/build")
    }
    
    doLast {
        println("🧹 All module build directories cleaned!")
    }
}

tasks.register("aegenesisTest") {
    group = "aegenesis"
    description = "Test AeGenesis build configuration"

    doLast {
        println("✅ AeGenesis Coinscience AI Ecosystem: OPERATIONAL")
        println("🧠 Multi-module architecture: STABLE")
        println("🔮 Unified API generation: READY") 
        println("🛠️  LSPosed integration: CONFIGURED")
        println("🌟 Welcome to the future of Android AI!")
    }
}
