plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.dokka")
    id("com.diffplug.spotless")

}

android {
    namespace = "dev.aurakai.auraframefx.dataveinoraclenative"

    // Required for native modules
    compileSdk = 36

    defaultConfig {
        minSdk = 33

        // NDK configuration for native code
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
        }

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++20", "-fPIC", "-O3")
                cppFlags("-O3", "-DNDEBUG", "-DDATA_VEIN_NATIVE_RELEASE")
                "-DANDROID_STL=c++_shared"
                "-DCMAKE_VERBOSE_MAKEFILE=ON"
                "-DDATA_VEIN_NATIVE_BUILD=ON"

            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
        viewBinding = false
    }
    dependencies {
        // Project modules
        implementation(project(":core-module"))

        // Core AndroidX
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // Compose - Genesis UI System
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.bundles.compose)
        implementation(libs.androidx.navigation.compose)

        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)
        implementation(libs.hilt.navigation.compose)

        // Coroutines - Genesis Async Processing
        implementation(libs.bundles.coroutines)

        // Kotlin reflection for KSP - fixed version
        implementation(libs.kotlin.reflect)

        // OpenAPI Generated Code Dependencies
        implementation(libs.retrofit)
        implementation(libs.retrofit.converter.kotlinx.serialization)
        implementation(libs.okhttp3.logging.interceptor)
        implementation(libs.kotlinx.serialization.json)

        // Core library desugaring
        coreLibraryDesugaring(libs.coreLibraryDesugaring)

        // Testing
        testImplementation(libs.junit.jupiter)
        androidTestImplementation(libs.androidx.test.ext.junit)
        androidTestImplementation(libs.espresso.core)

        // System interaction and documentation (using local JAR files)
        implementation(files("${project.rootDir}/Libs/api-82.jar"))
        implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))

    }
}