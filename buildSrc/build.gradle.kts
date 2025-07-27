plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(files("../gradle/libs.versions.toml"))
    implementation(libs.findPlugin("kotlin-android").get())
    implementation(libs.findPlugin("androidApplication").get())
}
