plugins {
    alias(libs.plugins.detekt)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("${rootDir}/config/detekt/detekt.yml"))
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}
