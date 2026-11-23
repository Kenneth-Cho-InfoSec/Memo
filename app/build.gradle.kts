import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "dev.arcturus.memo"
    compileSdk = 37
    ndkVersion = "28.2.13676358"

    defaultConfig {
        applicationId = "dev.arcturus.memo"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    lint {
        abortOnError = false
    }

    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

val ndkHome = System.getenv("ANDROID_NDK_HOME")
    ?: File(System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT") ?: "/home/kenneth/Android/Sdk", "ndk/28.2.13676358").absolutePath
val cargoBin = System.getenv("CARGO")
    ?: File(System.getProperty("user.home"), ".cargo/bin/cargo").takeIf { it.exists() }?.absolutePath
    ?: "cargo"
val rustTarget = "aarch64-linux-android"
val rustOut = rootProject.file("core/target/$rustTarget/release/liba64core.so")
val jniOut = rootProject.file("app/src/main/jniLibs/arm64-v8a/liba64core.so")

tasks.register<Exec>("cargoBuild") {
    group = "build"
    description = "Cross-compiles the Rust core for arm64-v8a."
    workingDir = rootProject.file("core")
    val ndkBin = File(ndkHome, "toolchains/llvm/prebuilt/linux-x86_64/bin").absolutePath
    val clang = File(ndkBin, "aarch64-linux-android21-clang").absolutePath
    val llvmAr = File(ndkBin, "llvm-ar").absolutePath
    val hostClang = "/tmp/ndk-host-bin/cc"
    environment("PATH", "/tmp/ndk-host-bin:$ndkBin:${System.getenv("PATH") ?: ""}")
    environment("ANDROID_NDK_HOME", ndkHome)
    environment("ANDROID_NDK_ROOT", ndkHome)
    environment("NDK_HOME", ndkHome)
    environment("CC", hostClang)
    environment("CC_x86_64_unknown_linux_gnu", hostClang)
    environment("CARGO_TARGET_X86_64_UNKNOWN_LINUX_GNU_LINKER", hostClang)
    environment("CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER", clang)
    environment("CC_aarch64_linux_android", clang)
    environment("AR_aarch64_linux_android", llvmAr)
    environment("RUSTFLAGS", "-C link-arg=-Wl,-z,max-page-size=16384")
    commandLine(cargoBin, "build", "--release", "--target", rustTarget)
    doLast {
        check(rustOut.exists()) { "Rust build did not produce ${rustOut.path}" }
        jniOut.parentFile.mkdirs()
        rustOut.copyTo(jniOut, overwrite = true)
    }
}

tasks.named("preBuild") {
    dependsOn("cargoBuild")
}

tasks.register<Delete>("cleanRust") {
    delete(rootProject.file("core/target"))
    delete(jniOut)
}
