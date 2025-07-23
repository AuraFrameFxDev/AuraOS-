// Top-level build file where you can add configuration options common to all sub-projects/modules.
// This version contains the corrected plugin alias syntax.

@Suppress("DSL_SCOPE_VIOLATION") // Suppress false positive warning for 'libs'
plugins {

    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.openapi.generator) apply false
}



tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}


