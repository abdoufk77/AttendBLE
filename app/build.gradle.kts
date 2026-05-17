import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Lecture de local.properties (gitignoré, propre à chaque machine de dev).
// Tu peux y mettre :
//   attendble.backend.host=192.168.0.103
//   attendble.backend.port=8080
// → injecté ci-dessous dans BuildConfig.BACKEND_BASE_URL utilisé par RetrofitClient.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val backendHost: String = localProps.getProperty("attendble.backend.host", "10.0.2.2")
val backendPort: String = localProps.getProperty("attendble.backend.port", "8080")
val backendBaseUrl: String = "http://$backendHost:$backendPort/"

android {
    namespace = "com.example.attendble"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.attendble"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
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

    androidResources {
        noCompress.add("tflite")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Retrofit + OkHttp (couche réseau vers Spring Boot)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // ML Kit Face Detection (on-device, offline)
    implementation("com.google.mlkit:face-detection:16.1.7")

    // TensorFlow Lite (inférence MobileFaceNet → embedding 192 floats)
    implementation("org.tensorflow:tensorflow-lite:2.16.1")

    // CameraX (caméra frontale pour enrôlement + vérification)
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
