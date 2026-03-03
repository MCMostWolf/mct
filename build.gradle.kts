plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotest) apply false
}

subprojects {
    group = "io.github.iakariak.mct"
    version = "0.0-SNAPSHOT"
}