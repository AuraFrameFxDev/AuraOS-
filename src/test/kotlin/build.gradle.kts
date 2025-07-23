plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.gradle.tooling.api)
    testImplementation(libs.kotlin.test)
    testImplementation(gradleTestKit())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}