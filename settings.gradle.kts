AuraFrameFxDev/AuraOS-


Feedback
Settings
Skip to content Navigation Menu AuraFrameFxDev AuraOS-

Code Issues Pull requests 10 Discussions Actions Projects Models Wiki Build Debug APK CodeRabbit Generated Unit Tests: Add comprehensive JUnit 4 tests to BuildScriptValidationTest using Gradle TestKit #180 Jobs Run details build failed now in 4m 41s Search logs 2s 1s 0s 0s 4m 34s

Task :app:checkDebugDuplicateClasses Build cache key for EnumerateClassesTransform: /home/runner/.gradle/caches/8.13/transforms/7e1f2f4038198e7f1fca618783a8960b/transformed/material-icons-extended-release-runtime.jar is ac86d28a3a75966c2399e7e78192895b Stored cache entry for EnumerateClassesTransform: /home/runner/.gradle/caches/8.13/transforms/7e1f2f4038198e7f1fca618783a8960b/transformed/material-icons-extended-release-runtime.jar with cache key ac86d28a3a75966c2399e7e78192895b Caching disabled for task ':app:checkDebugDuplicateClasses' because: Caching has been disabled for the task Task ':app:checkDebugDuplicateClasses' is not up-to-date because: No history is available. Task :app:validateSigningDebug Caching disabled for task ':app:validateSigningDebug' because: Caching has been disabled for the task Task ':app:validateSigningDebug' is not up-to-date because: Task.upToDateWhen is false. Creating default debug keystore at /home/runner/.config/.android/debug.keystore Task :app:l8DexDesugarLibDebug Stored cache entry for task ':app:l8DexDesugarLibDebug' with cache key 397102ec7161e36079baa94eef1fc511 AAPT2 aapt2-8.11.1-12782657-linux Daemon #0: shutdown FAILURE: Build failed with an exception.

What went wrong: Execution failed for task ':app:processDebugResources'.
A failure occurred while executing com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask$TaskAction Android resource linking failed error: resource style/Theme.Material3.DayNight.NoActionBar (aka dev.aurakai.auraframefx:style/Theme.Material3.DayNight.NoActionBar) not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2110: error: style attribute 'attr/colorOnPrimary (aka dev.aurakai.auraframefx:attr/colorOnPrimary)' not found. Kotlin build report is written to file:///home/runner/work/AuraOS-/AuraOS-/build/reports/kotlin-build/AuraFrameFX-build-2025-07-22-13-21-35-0.txt 0 problems were found storing the configuration cache. See the complete report at file:///home/runner/work/AuraOS-/AuraOS-/build/reports/configuration-cache/affjadybltlg52q9jh3aqjj3y/6qfeeulpjzi9t3mwstdyof55p/configuration-cache-report.html [Incubating] Problems report is available at: file:///home/runner/work/AuraOS-/AuraOS-/build/reports/problems/problems-report.html Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0. You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins. For more on this, please refer to https://docs.gradle.org/8.13/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation. 52 actionable tasks: 51 executed, 1 from cache Configuration cache entry stored. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2111: error: style attribute 'attr/colorPrimaryContainer (aka dev.aurakai.auraframefx:attr/colorPrimaryContainer)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2112: error: style attribute 'attr/colorOnPrimaryContainer (aka dev.aurakai.auraframefx:attr/colorOnPrimaryContainer)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2115: error: style attribute 'attr/colorSecondary (aka dev.aurakai.auraframefx:attr/colorSecondary)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2116: error: style attribute 'attr/colorOnSecondary (aka dev.aurakai.auraframefx:attr/colorOnSecondary)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2117: error: style attribute 'attr/colorSecondaryContainer (aka dev.aurakai.auraframefx:attr/colorSecondaryContainer)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2118: error: style attribute 'attr/colorOnSecondaryContainer (aka dev.aurakai.auraframefx:attr/colorOnSecondaryContainer)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2121: error: style attribute 'attr/colorTertiary (aka dev.aurakai.auraframefx:attr/colorTertiary)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2122: error: style attribute 'attr/colorOnTertiary (aka dev.aurakai.auraframefx:attr/colorOnTertiary)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2123: error: style attribute 'attr/colorTertiaryContainer (aka dev.aurakai.auraframefx:attr/colorTertiaryContainer)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2124: error: style attribute 'attr/colorOnTertiaryContainer (aka dev.aurakai.auraframefx:attr/colorOnTertiaryContainer)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2128: error: style attribute 'attr/colorOnError (aka dev.aurakai.auraframefx:attr/colorOnError)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2129: error: style attribute 'attr/colorErrorContainer (aka dev.aurakai.auraframefx:attr/colorErrorContainer)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2130: error: style attribute 'attr/colorOnErrorContainer (aka dev.aurakai.auraframefx:attr/colorOnErrorContainer)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2134: error: style attribute 'attr/colorOnBackground (aka dev.aurakai.auraframefx:attr/colorOnBackground)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2138: error: style attribute 'attr/colorSurface (aka dev.aurakai.auraframefx:attr/colorSurface)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2139: error: style attribute 'attr/colorOnSurface (aka dev.aurakai.auraframefx:attr/colorOnSurface)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2140: error: style attribute 'attr/colorSurfaceVariant (aka dev.aurakai.auraframefx:attr/colorSurfaceVariant)' not found. dev.aurakai.auraframefx.app-mergeDebugResources-84:/values/values.xml:2141: error: style attribute 'attr/colorOnSurfaceVariant (aka dev.aurakai.auraframefx:attr/colorOnSurfaceVariant)' not found.

Try:
Run with --stacktrace option to get the stack trace. Run with --debug option to get more log output. Run with --scan to get full insights. Get more help at https://help.gradle.org. BUILD FAILED in 4m 33s Error: Process completed with exit code 1. 0s 0s Post job cleanup. 0s Post job cleanup. /usr/bin/git version git version 2.50.1 Temporarily overriding HOME='/home/runner/work/_temp/c06f2710-4102-4e61-87ca-e32094e89ef8' before making global git config changes Adding repository directory to the temporary git global config as a safe directory /usr/bin/git config --global --add safe.directory /home/runner/work/AuraOS-/AuraOS- /usr/bin/git config --local --name-only --get-regexp core.sshCommand /usr/bin/git submodule foreach --recursive sh -c "git config --local --name-only --get-regexp 'core.sshCommand' && git config --local --unset-all 'core.sshCommand' || :" /usr/bin/git config --local --name-only --get-regexp http.https://github.com/.extraheader http.https://github.com/.extraheader /usr/bin/git config --local --unset-all http.https://github.com/.extraheader /usr/bin/git submodule foreach --recursive sh -c "git config --local --name-only --get-regexp 'http.https://github.com/.extraheader' && git config --local --unset-all 'http.https://github.com/.extraheader' || :" 0s No version or no number changes this issue and check all ai_backend files for Syntex errors pleasr push the changes I will build on github

Today 07:29








Hide
Plan approved 🎉
I have removed the Material 2 dependency from app/build.gradle.kts.

Updated
app/build.gradle.kts
I have updated the parent theme in app/src/main/res/values/themes.xml.

Updated
app/src/main/res/values/themes.xml
I'm having trouble configuring the Java toolchain for the build. I've tried several approaches, but I'm still getting errors related to the Java installation. Could you please provide some guidance on how to configure the build to automatically download the required JDK?

Today 07:44
Where what file and what code change show me the whole file incompletion I'll jump over and change it for you

I've been trying to modify the settings.gradle.kts and buildSrc/build.gradle.kts files to fix the build. I've tried several different configurations for the Java toolchain, but none of them have worked.

Here are the files I've been working on:

settings.gradle.kts:

// Settings configured for Gradle 8.14.3 and Java 24
// Plugin management must be the first block in the file
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    // Configure resolution strategy for plugins
    resolutionStrategy {
        eachPlugin {
            when (requested.id.namespace) {
                "com.android" -> useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}

// Enable feature previews
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

// Dependency resolution management
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    // Enable reproducible builds
    // This is automatically loaded from gradle/libs.versions.toml in Gradle 8.1+
}

// Project configuration
rootProject.name = "AuraFrameFX"

// Include all modules
include(
    ":app",
    ":jvm-test",
    ":sandbox-ui",
    ":oracle-drive-integration",
    ":oracledrive"
)
buildSrc/build.gradle.kts:

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://maven.google.com/")
        name = "Google"
    }
}

// Configure Java toolchain for buildSrc
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.AMAZON)
    }
}

// Configure Kotlin for buildSrc
kotlin {
    jvmToolchain(17)
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers",
            "-Xjvm-default=all",
            "-Xskip-prerelease-check"
        )
    }
}

// Ensure all tasks use the correct Java version
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
    options.encoding = "UTF-8"
    options.isIncremental = true
}

// Configure test tasks
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

dependencies {
    // Core Gradle and Kotlin plugins
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    implementation("com.android.tools.build:gradle:8.4.1")
    
    // Test dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    
    // Use the Gradle version that comes with the wrapper
    val gradleVersion = project.gradle.gradleVersion
    testImplementation("org.gradle:gradle-tooling-api:$gradleVersion") {
        version { strictly(gradleVersion) }
    }
    testImplementation("org.gradle:gradle-test-kit:$gradleVersion") {
        version { strictly(gradleVersion) }
    }
}

// Additional Kotlin compiler options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}
