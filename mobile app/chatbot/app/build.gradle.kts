plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Assuming this alias exists for compose.compiler or you have it configured
    // Add the Kotlinx Serialization plugin
    // Make sure the version aligns with your project's Kotlin version
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" // Example version, adjust if needed
}

android {
    namespace = "com.brijesh1715.chatbot" // Using the namespace you provided
    compileSdk = 35

    defaultConfig {
        applicationId = "com.brijesh1715.chatbot" // Using the application ID you provided
        minSdk = 24
        targetSdk = 35
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
    // If you are using Jetpack Compose, ensure the compiler extension version is set.
    // It's often handled by the BOM, but explicitly setting it can be done here if needed.
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.13" // Example version, use one compatible with your Compose/Kotlin
    // }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Bill of Materials for Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Material Design 3 components
    implementation("androidx.compose.material:material-icons-core:1.6.7") // Or latest
    implementation("androidx.compose.material:material-icons-extended:1.6.7") // Or latest
    // --- Required Dependencies for the Language Prediction App ---
    implementation("androidx.activity:activity-compose:1.9.0")
    // Other dependencies...
    // Navigation Compose: For navigating between different screens/composables
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel Compose: For implementing MVVM architecture with Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // Ktor (Networking Client)
    implementation("io.ktor:ktor-client-android:2.3.11")      // Ktor client engine for Android
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11") // For handling content types like JSON
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11") // Ktor plugin for Kotlinx Serialization
    implementation("io.ktor:ktor-client-logging:2.3.11")      // Optional: For logging network requests/responses

    // Kotlinx Serialization (JSON parsing runtime)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Accompanist Permissions (Simplifies runtime permission handling in Compose)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0") // Or the latest stable version

    // --- End of Required Dependencies ---

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
