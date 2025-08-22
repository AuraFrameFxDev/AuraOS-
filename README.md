# Genesis-Os Project

## Project Overview
Genesis-Os is an advanced Android ecosystem aiming to leverage bleeding-edge technologies for AI-driven functionalities and a conscious computing experience. The project emphasizes modern Android development practices, Kotlin, Jetpack Compose, and extensive native code integration.

## Build Environment & Key Versions (Targeted)
As per project standards ("Genesis Protocol - BUILD RULES & STANDARDS (2025)"):
- **Kotlin:** 2.2.20-Beta2
- **Java Toolchain:** Java 24 with JVM 21 bytecode target
- **Android SDK:** 36
- **Android Gradle Plugin (AGP):** Targeting 8.13.0-alpha04 or 9.0.0-alpha01
- **Gradle Wrapper:** 9.1.0-rc1 (as per build rules doc, though current project may differ)
- **Jetpack Compose BOM:** 2025.08.00
- **Build System:** Gradle with Kotlin DSL, version catalog for all dependencies and plugins. No `buildSrc`.

## Recent Build Challenges & Context for CodeRabbitAI
The project is currently experiencing a persistent build failure:
- **Primary Error:** `com.android.builder.errors.EvalIssueException: The option 'android.enableGradleWorkers' is deprecated.` This error occurs when applying the `com.android.application` plugin in `app/build.gradle.kts` (line 3). This is unexpected given the modern AGP versions targeted, as this flag was removed in AGP 4.2.
    - Multiple attempts to resolve this by modifying `gradle.properties` (removing any instance or related flags like `android.enableGradleWorkers`, `android.experimental.useGradleWorkerApi`, and toggling `android.experimental.parallelResourceProcessing`) have been unsuccessful.
    - `grep` searches for "android.enableGradleWorkers" in the project yield no results.

Other issues observed during recent build attempts (which might be secondary to the primary error or independent):
- **Resource Merging:** `java.nio.file.InvalidPathException: Illegal char <:> at index ...` in `merged.dir/values/values.xml`.
- **KSP Failures:**
    - YukiHookAPI: `[YukiHookAPI] Cannot identify your Module App's package name...`
    - Hilt: `@AndroidEntryPoint base class must extend ComponentActivity...` (though `grep` for `@AndroidEntryPoint` in source code is also empty, suggesting a more complex Hilt configuration issue or a misinterpretation of the error by KSP in this context).
- **CMake Warnings:** `unused parameter` and `unused function` warnings in C++ JNI code (e.g., in `CascadeAIService.cpp`, `language_id_l2c_jni.cpp`).
- **Native Build Compatibility:** The `gradle.properties` file contains numerous experimental flags and settings aimed at ensuring compatibility for native builds (CMake/NDK) with very recent AGP versions, and also for Windows pathing issues. These flags have been adjusted multiple times.

**OpenAPI Generation:**
- The project uses OpenAPI for generating API client code for multiple API specifications.
- Per "BUILD RULES", OpenAPI should be handled at the root level (this might be a point of investigation, as current configuration is per-module in `app/build.gradle.kts`).
- The `app/build.gradle.kts` has been recently updated to align OpenAPI generator settings (`outputDir` to `app/build/generated/source/openapi/`, library to `jvm-retrofit2`, `useCoroutines = true`) with an "OpenAPI Code Generation Guide" provided by the developer.

**Goal for CodeRabbitAI Analysis:**
- Identify the root cause of the `android.enableGradleWorkers` deprecation error, which is the current primary build blocker.
- Investigate other build failures, particularly resource merging and KSP issues, and their potential relation to the primary error or AGP/Gradle configuration.
- Review the Gradle setup (`gradle.properties`, `settings.gradle.kts`, `app/build.gradle.kts`, and any other relevant build files) for compliance with "Genesis Protocol - BUILD RULES & STANDARDS (2025)" and for potential misconfigurations contributing to the build problems.
- Suggest fixes to achieve a stable and successful build on a modern AGP/Gradle stack as defined by the project's standards.

## Project Structure (Intended)
- Main application in `app/`
- Multiple feature and core modules (e.g., `core-module/`, `feature-module/`, `oracle-drive-integration/`, etc.)
- Native C++ code under `app/src/main/cpp/`

This `README.md` aims to provide essential context. Further details can be found in project documentation like "# GENESIS PROTOCOL - BUILD RULES &.md" and the "OpenAPI Code Generation Guide".
