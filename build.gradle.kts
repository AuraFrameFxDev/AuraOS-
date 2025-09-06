// ==== GENESIS PROTOCOL - ROOT BUILD CONFIGURATION ====
// TOML validation successful - structure is correct

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
    alias(libs.plugins.openapi.generator) apply false
}

tasks.register("validateToml") {
    doLast {
        println("🧠 Testing TOML Version Catalog validation...")
        try {
            println("AGP version: " + libs.versions.agp.get())
            println("Kotlin version: " + libs.versions.kotlin.get()) 
            println("Compose BOM version: " + libs.versions.composeBom.get())
            println("Hilt version: " + libs.versions.hilt.get())
            
            println("✅ TOML validation successful!")
        } catch (e: Exception) {
            println("❌ TOML validation failed: " + e.message)
            throw e
        }
    }
}