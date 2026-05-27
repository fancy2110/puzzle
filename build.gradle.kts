plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}

// Task to build native Rust library for Android
tasks.register("buildNativeAndroid") {
    group = "native"
    description = "Build puzzle-core .so files for Android (requires Rust + NDK)"
    doLast {
        exec {
            workingDir = file("native")
            commandLine("bash", "build_android.sh")
        }
    }
}

// Task to build native Rust library for iOS
tasks.register("buildNativeIos") {
    group = "native"
    description = "Build puzzle-core .a / XCFramework for iOS (requires Rust + Xcode)"
    doLast {
        exec {
            workingDir = file("native")
            commandLine("bash", "build_ios.sh")
        }
    }
}

// Build all native libs
tasks.register("buildNativeAll") {
    group = "native"
    description = "Build native libs for both Android and iOS"
    dependsOn("buildNativeAndroid", "buildNativeIos")
}
