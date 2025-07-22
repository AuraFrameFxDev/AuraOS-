plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

// Define the specific versions required for the buildSrc module itself.
val kotlinVersion = "2.2.0"
val agpVersion = "8.11.1" 

// Configure Kotlin compilation for the buildSrc module
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        
        // CORRECTED: Using freeCompilerArgs is a more direct and robust way to 
        // set the JVM target, preventing the fallback to JVM 22.
        freeCompilerArgs.add("-Xjvm-target=22")
    }
}

// Configure Java compilation for the buildSrc module
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "24"
    targetCompatibility = "24"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$agpVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

// Configure the test runner for buildSrc tests
tasks.withType<Test> {
    useJUnitPlatform()
}

