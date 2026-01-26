import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.gvteam.sisimpresion3d"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.gvteam.sisimpresion3d"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val keystoreFile = project.rootProject.file("local.properties")
        if (keystoreFile.exists())
            properties.load(FileInputStream(keystoreFile))
        val supabaseUrl = properties.getProperty("SUPABASE_URL") ?: ""
        val supabaseKey = properties.getProperty("SUPABASE_KEY") ?: ""
        val googleClientID = properties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleClientID\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.credentials)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Supabase
    implementation(platform(libs.bom))
    implementation(libs.postgrest.kt) // Base de Datos
    implementation(libs.realtime.kt) // Tiempo real
    implementation(libs.auth.kt)   // Login

    // Motor HTTP Ktor
    implementation(libs.ktor.client.android)

    // Serializador JSON
    implementation(libs.kotlinx.serialization.json)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Navegacion
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Haze (Efecto Glass)
    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Ktor HTTP
    implementation(libs.ktor.client.okhttp)

    // Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // Google Identity
    implementation(libs.googleid)

    // AsyncImage
    implementation(libs.coil.compose)

    // Hero Animation
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.navigation.compose)

    // Storage
    implementation(libs.storage.kt)

    // Recortes
    implementation("com.vanniktech:android-image-cropper:4.7.0")
}