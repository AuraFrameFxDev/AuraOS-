plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

kotlin {
    jvmToolchain(24)
}

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.assertj.core)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

kover {
    isDisabled = false
    engine.set(kotlinx.kover.api.DefaultIntellijEngine.v1_9_10)

    filters {
        classes {
            includes += "dev.aurakai.auraframefx.*"
        }
    }

    verify {
        rule {
            isEnabled = true
            bound {
                minValue = 80
                maxValue = 100
                metric = kotlinx.kover.api.VerificationCoverageType.LINE
                aggregation = kotlinx.kover.api.VerificationCoverageAggregation.COVERED_PERCENTAGE
            }
        }
    }
}