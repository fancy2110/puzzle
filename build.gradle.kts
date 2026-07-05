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
        providers.exec {
            workingDir = file("native")
            commandLine("bash", "build_android.sh")
        }.result.get()
    }
}

// Task to build native Rust library for iOS
tasks.register("buildNativeIos") {
    group = "native"
    description = "Build puzzle-core .a / XCFramework for iOS (requires Rust + Xcode)"
    doLast {
        providers.exec {
            workingDir = file("native")
            commandLine("bash", "build_ios.sh")
        }.result.get()
    }
}

// Build all native libs
tasks.register("buildNativeAll") {
    group = "native"
    description = "Build native libs for both Android and iOS"
    dependsOn("buildNativeAndroid", "buildNativeIos")
}
