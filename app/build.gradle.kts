plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt") // Para el procesamiento de anotaciones, necesario para algunas librerías de Compose y Room
}

android {
    namespace = "com.example.vuelink"
    compileSdk = 34  // Utilizamos la API 34 de Android (Android 14.0)

    defaultConfig {
        applicationId = "com.example.vuelink"
        minSdk = 26  // Configura la mínima versión de Android soportada
        targetSdk = 34  // Orientación a Android 14.0 (API 34)
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
        jvmTarget = "17" // Esto asegura que Kotlin también se compile con Java 17
    }

    buildFeatures {
        compose = true  // Habilita Jetpack Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"  // Asegúrate de usar la versión de Kotlin y Compose compatibles
        kotlinCompilerVersion = "1.9.21"  // La versión de Kotlin que estás utilizando
    }
}

dependencies {
    // Dependencias principales para la app
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Dependencias de Jetpack Compose
    implementation("androidx.compose.ui:ui:1.4.0")  // Dependencia de UI de Compose
    implementation("androidx.compose.material3:material3:1.0.0")  // Material 3 para Compose
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")  // Herramientas de vista previa
    implementation("androidx.activity:activity-compose:1.9.3")  // Necesaria para las Activity y Compose
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    // Para manejar solicitudes HTTP
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Para la conversión de JSON a objetos Kotlin
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.junit.junit)

    implementation("androidx.room:room-runtime:2.5.1")
    kapt("androidx.room:room-compiler:2.5.1")

}
