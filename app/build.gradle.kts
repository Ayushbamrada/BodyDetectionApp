// build.gradle.kts (app level)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.bodydetectionapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bodydetectionapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    // Added for ML Kit model integration to prevent compression of .tflite files
    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    // Keep libs.androidx.lifecycle.runtime.ktx as it's defined in your libs.versions.toml
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Keep the Compose BOM for consistent versions
    implementation(libs.androidx.ui) // Will use version from BOM
    implementation(libs.androidx.ui.graphics) // Will use version from BOM
    implementation(libs.androidx.ui.tooling.preview) // Will use version from BOM
    implementation(libs.androidx.material3) // Will use version from BOM

    // CameraX dependencies (Keeping your specified versions as requested)
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-mlkit-vision:1.4.2")
    implementation("com.google.code.gson:gson:2.10.1")

    // ML Kit Pose Detection Accurate (Keeping your specified beta version as requested)
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta5")

    // MediaPipe (Keeping your specified version as requested)
    implementation("com.google.mediapipe:tasks-vision:0.10.11") // Correct, stable version
    // IMPORTANT: For PoseLandmarker, you usually need the specific artifact.
    // The previously commented out line `tasks-vision-poselandmarker:0.10.11` is old.
    // If you are using `PoseLandmarkerResult` directly, you need this:
//    implementation("com.google.mediapipe:tasks-vision-poselandmarker:0.10.26") // Align version with tasks-vision.

    // ViewModel utilities for Compose
    // Keeping this version as requested, it's the one that provides `collectAsState`.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    // Keep kotlinx-coroutines-android for coroutine dispatchers and scope support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Coil (for image loading - keeping your specified version)
    implementation("io.coil-kt:coil-compose:2.6.0")
    // In your dependencies { ... } block
    implementation("androidx.compose.material:material-icons-extended-android:1.6.8") // Use the version that matches your other compose libraries

    // --- REMOVED DUPLICATE AND CONFLICTING DEPENDENCIES ---
    // Removed: implementation("androidx.compose.ui:ui:1.6.0") // Conflicts with BOM
    // Removed: implementation("androidx.compose.material3:material3:1.2.0") // Conflicts with BOM
    // Removed: implementation("androidx.navigation:navigation-compose:2.7.5") // Duplicate of 2.7.7
    // Removed: implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // Duplicate/Older of libs.androidx.lifecycle.runtime.ktx
    // Removed: implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2") // Duplicate/Older of 2.7.0

    // Jetpack Compose Navigation: You had 2.7.7 commented out and 2.7.5 active.
    // For consistency and newer features, you should use one. I'll uncomment and activate 2.7.7.
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.genai.common)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-gif:2.6.0")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}