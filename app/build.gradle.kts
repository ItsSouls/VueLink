plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt") // Required for annotation processing
}

android {
    namespace = "es.uma.vuelink"
    compileSdk = 34  // Targeting Android 14.0

    defaultConfig {
        applicationId = "es.uma.vuelink"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"  // Correct version for Kotlin 1.9.10 and Compose 1.6.0
    }
}

dependencies {
    // App essentials
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Jetpack Compose dependencies
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation("androidx.compose.foundation:foundation:1.6.0") // Para LazyColumn y otros componentes
    implementation("androidx.compose.runtime:runtime-livedata:1.6.1")

    // Networking and JSON
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.code.gson:gson:2.8.9")

    // Room dependencies
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.cardview)
    kapt("androidx.room:room-compiler:2.6.1")

    // Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
