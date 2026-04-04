@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeBinaryContainer

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
    alias(libs.plugins.shadow)
}

kotlin {
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }

        mainRun {
            mainClass.set("mct.cli.MainKt")
        }
    }

    val exeConfigure: KotlinNativeBinaryContainer.() -> Unit = {
        executable {
            baseName = "mct"
            entryPoint = "mct.cli.main"
        }
    }
    mingwX64 {
        binaries(exeConfigure)
    }
    linuxX64 {
        binaries(exeConfigure)
    }

    tasks.named<Jar>("jvmJar") {
        manifest {
            attributes["Main-Class"] = "mct.cli.MainKt"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.clikt)
            implementation(project(":mct"))
        }

        commonTest.dependencies {
            implementation(libs.bundles.kotest)
        }

        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
        }
    }
}
