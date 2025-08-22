import org.gradle.kotlin.dsl.composeCompiler

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compose)
    id("org.openapi.generator") version "7.14.0"
}

android {
    namespace = "dev.aurakai.auraframefx"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++20", "-fPIC", "-O3")
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DCMAKE_VERBOSE_MAKEFILE=ON",
                    "-DGENESIS_BUILD=ON"
                )
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
        prefab = false
    }

    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        includeSourceInformation = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs(
                layout.buildDirectory.dir("generated/source/openapi/src/main/kotlin")
            )
        }
    }
    buildToolsVersion = "36.0.0"
}

// ===== STANDARD OPENAPI CONFIGURATION =====
// Following the documented approach from docs/OpenAPI-Generation-Guide.md

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(file("../openapi/specs/genesis-api.yml").toURI().toString())
    outputDir.set(layout.buildDirectory.dir("generated/source/openapi").get().asFile.toString())
    packageName.set("dev.aurakai.genesis.api")
    apiPackage.set("dev.aurakai.genesis.api")
    modelPackage.set("dev.aurakai.genesis.model")
    invokerPackage.set("dev.aurakai.genesis.client")
    skipOverwrite.set(false)
    validateSpec.set(false)
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
    configOptions.set(mapOf(
        "library" to "jvm-retrofit2",
        "useCoroutines" to "true",
        "serializationLibrary" to "kotlinx_serialization",
        "dateLibrary" to "kotlinx-datetime",
        "sourceFolder" to "src/main/kotlin"
    ))
}

// Build integration with proper ordering
tasks.named("preBuild") {
    dependsOn("openApiGenerate")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
    mustRunAfter("openApiGenerate")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.timber)
    implementation(libs.coil.compose)

    coreLibraryDesugaring(libs.coreLibraryDesugaring)
    implementation(libs.androidxSecurity)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.firebase.bom)
    // TODO: Add Firebase services when needed:
    // implementation("com.google.firebase:firebase-analytics-ktx")
    // implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Xposed Framework - YukiHookAPI (Standardized)
    implementation(libs.yuki)
    implementation(libs.bundles.xposed)
    ksp(libs.yuki.ksp.xposed)
    implementation(fileTree(mapOf("dir" to "../Libs", "include" to listOf("*.jar"))))

    debugImplementation(libs.leakcanary.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.engine)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
