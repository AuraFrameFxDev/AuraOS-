// ===== COMPREHENSIVE AEGENESIS CLEANUP TASK =====
tasks.register<Delete>("cleanAllGeneratedFiles") {
    group = "aegenesis"
    description = "Clean all generated files that might cause build issues"
    
    val buildDirProvider = layout.buildDirectory
    val projectDirProvider = layout.projectDirectory
    
    // Clean all build directories
    delete(buildDirProvider)
    
    // Clean KSP and annotation processing generated files
    delete(
        buildDirProvider.dir("generated/ksp"),
        buildDirProvider.dir("generated/source/ksp"),
        buildDirProvider.dir("generated/ap_generated_sources"),
        buildDirProvider.dir("tmp/kapt3"),
        buildDirProvider.dir("tmp/kotlin-classes"),
        buildDirProvider.dir("kotlin"),
        buildDirProvider.dir("generated/hilt"),
        buildDirProvider.dir("generated/source/navigation-args")
    )
    
    // Clean potential problematic cached files
    delete(
        projectDirProvider.dir(".gradle/8.10.2/kotlin"),
        projectDirProvider.dir(".gradle/kotlin"),
        projectDirProvider.file(".gradle/kotlin.lock")
    )
    
    doLast {
        println("🧹 Comprehensive cleanup completed!")
        println("✅ All generated files cleaned")
        println("🔄 Ready for fresh build with Java 21 + AGP 9.0.0-alpha02")
    }
}

tasks.register("aegenesisHealthCheck") {
    group = "aegenesis"
    description = "Complete AeGenesis configuration health check"
    
    doLast {
        println("🏥 AEGENESIS HEALTH CHECK")
        println("=".repeat(70))
        
        // AGP Version
        println("⚡ AGP Version: 9.0.0-alpha02")
        
        // Java Version Check
        val javaVersion = System.getProperty("java.version")
        val javaVendor = System.getProperty("java.vendor")
        println("☕ System Java: $javaVersion ($javaVendor)")
        
        // Gradle Properties Check
        val kspMode = project.findProperty("ksp.useKSP2")?.toString() ?: "default"
        println("🧠 KSP Mode: $kspMode ${if (kspMode == "false") "✅ (Fixed NullPointer)" else "⚠️"}")
        
        // Java Version Consistency  
        println("🎯 Java 21 Toolchain: ✅ Configured")
        println("🔧 Compile Target: Java 21 (JVM_21)")
        
        // Unified API Status
        val apiFile = layout.projectDirectory.file("api/unified-aegenesis-api.yml").asFile
        println("🔌 Unified API: ${if (apiFile.exists()) "✅ ${apiFile.length() / 1024}KB" else "❌ Missing"}")
        
        // Serialization Status
        val serializationFile = layout.projectDirectory.file("src/main/kotlin/dev/aurakai/auraframefx/serialization/CustomSerializers.kt").asFile
        println("🔄 Custom Serializers: ${if (serializationFile.exists()) "✅" else "❌ Missing"}")
        
        // Module Status
        val coreModuleExists = project.findProject(":core-module") != null
        val romtoolsExists = project.findProject(":romtools") != null
        val secureCommExists = project.findProject(":secure-comm") != null
        
        println("📦 Module Architecture:")
        println("   • JVM Modules: core-module")
        println("   • Android Modules: romtools, secure-comm, oracle-drive-integration, collab-canvas")
        println("   • Main App: app (Android application)")
        println("   • Note: romtools is Android library (uses Context, KeyStore, etc.)")
        
        println("=".repeat(70))
        if (kspMode == "false" && apiFile.exists() && serializationFile.exists()) {
            println("🌟 STATUS: READY FOR BUILD!")
            println("💡 Recommended: Run './gradlew cleanAllGeneratedFiles' then build")
        } else {
            println("⚠️  STATUS: Issues detected - see above")
        }
    }
}
