plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "es.uma.vuelink"
    compileSdk = 34

    defaultConfig {
        applicationId = "es.uma.vuelink"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
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
}

dependencies {
    implementation(libs.activity)
    implementation(libs.activity.compose)
    implementation(libs.annotation)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.text)
    implementation(libs.compose.ui.unit)
    implementation(libs.constraintlayout)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.maps.compose)
    implementation(libs.navigation.common)
    implementation(libs.navigation.compose)
    implementation(libs.navigation.runtime)
    implementation(libs.okhttp)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.room.common)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(libs.runtime.saveable)
    implementation(libs.sqlite)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.test.monitor)
}
