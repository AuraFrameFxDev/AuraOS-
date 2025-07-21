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

// Configure Java toolchain for all tasks in buildSrc
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

dependencies {
    // Core Gradle and Kotlin plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.2.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

    // Use the Gradle version that comes with the wrapper for test kit
    val gradleVersion = project.gradle.gradleVersion
    testImplementation("org.gradle:gradle-tooling-api:$gradleVersion") {
        version { strictly(gradleVersion) }
    }
    testImplementation("org.gradle:gradle-test-kit:$gradleVersion") {
        version { strictly(gradleVersion) }
    }
}

// Configure all Kotlin compilation tasks
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        // The JVM target is inferred from the toolchain.
        // If you need to set it explicitly for any reason, use the enum.
        // jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22) // Example, but toolchain is preferred

        // Add other necessary compiler arguments here
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xskip-prerelease-check" // Add this if you encounter prerelease errors
        )
    }
}

// Ensure all tasks use the correct Java version
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_24.toString()
    targetCompatibility = JavaVersion.VERSION_24.toString()
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.release.set(24)
}

// Configure test tasks
tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
    testLogging {
        events("passed", "skipped", "failed")
    }
}