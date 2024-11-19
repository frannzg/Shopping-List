plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.frannzg.shopping_list"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.frannzg.shopping_list"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Dependencia para Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:20.0.3")

    // Dependencia para Firebase Authentication
    implementation("com.google.firebase:firebase-auth:21.0.3")


    implementation ("com.google.android.material:material:1.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Aplicar el plugin de Google Services
apply(plugin = "com.google.gms.google-services")


