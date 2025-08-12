// Genesis-OS Root Build - Auto-Provisioned Everything
// Sacred Rule: "NO hardcoded versions, NO manual toolchains"

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false  
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.openapi.generator) apply false
}

// Auto-Provisioned OpenAPI Generation (Force Clean & Regenerate)
allprojects {
    tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask> {
        // Force clean and regenerate on every build/sync
        outputs.upToDateWhen { false }
        doFirst {
            delete(outputDir)
        }
    }
    
    // Hook into preBuild for automatic execution
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn(tasks.withType<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>())
    }
}

// Auto-Provisioned Clean Task
tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
    
    // Clean all module build directories
    subprojects.forEach { subproject ->
        delete(subproject.layout.buildDirectory)
    }
}