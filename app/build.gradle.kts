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
        minSdk = 24
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // CameraX dependencies - Update to 1.3.1 (stable) or 1.3.0-beta02 if 1.3.0 doesn't resolve
    // You are using 1.3.0 for core, camera2, lifecycle, view. Let's keep these consistent.
    // However, camera-mlkit-vision had specific issues with 1.3.0. Let's use 1.4.2 stable or a beta.
    implementation("androidx.camera:camera-core:1.3.1") // Consider using 1.3.1 or a newer stable version
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-mlkit-vision:1.4.2") // Updated to latest stable version
    // If you explicitly want to target 1.3.x for everything, then use:
    // implementation("androidx.camera:camera-mlkit-vision:1.3.0-beta02") // This was the last beta for 1.3.x

    // ML Kit Pose Detection Accurate - **Crucial Update**
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta5") // Updated to the latest beta version that resolves

    // Mediapipe
    implementation("com.google.mediapipe:tasks-vision:0.10.26")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}