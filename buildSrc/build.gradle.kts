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
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

// Configure Kotlin for buildSrc
kotlin {
    jvmToolchain(24)
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
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

dependencies {
    // Core Gradle and Kotlin plugins
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    implementation("com.android.tools.build:gradle:8.5.1")
    
    // Test dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.0")
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
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}