import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        iosTarget.compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
        // CInterop for puzzle-core native library
        iosTarget.compilations.getByName("main").cinterops {
            val puzzleCore by creating {
                defFile(project.file("../native/puzzle_core.def"))
                packageName("puzzle_core")
                includeDirs(
                    project.file("../native/puzzle-core/include")
                )
            }
        }
    }

    // JNI libs directory for Android
    sourceSets {
        androidMain {
            resources.srcDirs("src/androidMain/jniLibs")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.uuid)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(project(":logger"))
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.umeng.common)
            implementation(libs.umeng.asms)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.puzzle.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.puzzle.game"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        manifestPlaceholders["UMENG_APP_KEY"] = providers.gradleProperty("UMENG_ANDROID_APP_KEY").orElse("").get()
        manifestPlaceholders["UMENG_CHANNEL"] = providers.gradleProperty("UMENG_CHANNEL").orElse("official").get()
        manifestPlaceholders["UMENG_ANALYTICS_ENABLED"] = providers.gradleProperty("UMENG_ANALYTICS_ENABLED").orElse("false").get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

// Copy demo resources into iOS framework bundle
tasks.register<Copy>("copyIosResources") {
    from("src/commonMain/composeResources/files")
    into("${buildDir}/ios-framework-resources")
}
tasks.matching { it.name.startsWith("link") && it.name.contains("FrameworkIos") }.configureEach {
    dependsOn("copyIosResources")
}

// ── iOS Simulator Run ────────────────────────────────────

tasks.register("iosSimulatorArm64Run") {
    group = "ios"
    description = "Build framework and launch iOS simulator"
    dependsOn("linkDebugFrameworkIosSimulatorArm64")
    doLast {
        val xcrun = "/usr/bin/xcrun"
        val appPath = "${rootProject.projectDir}/iosApp/iosApp.xcodeproj"

        // Boot simulator if not running
        exec {
            commandLine(xcrun, "simctl", "boot", "iPhone 17 Pro")
            isIgnoreExitValue = true
        }
        // Open simulator
        exec {
            commandLine("open", "-a", "Simulator")
        }
        // Build and run via xcodebuild
        exec {
            workingDir = file("${rootProject.projectDir}/iosApp")
            commandLine(
                xcrun, "xcodebuild",
                "-project", "iosApp.xcodeproj",
                "-scheme", "iosApp",
                "-configuration", "Debug",
                "-destination", "platform=iOS Simulator,name=iPhone 17 Pro",
                "build"
            )
        }
        // Install and launch
        exec {
            commandLine(xcrun, "simctl", "install", "booted",
                "${rootProject.projectDir}/iosApp/build/Debug-iphonesimulator/iosApp.app"
            )
        }
        exec {
            commandLine(xcrun, "simctl", "launch", "booted", "com.puzzle.game")
        }
    }
}
