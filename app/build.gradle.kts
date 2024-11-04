plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.matchmakers"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.matchmakers"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
//    Firebase BoM ensures you always get the compatible versions
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Room components
    val room_version = "2.6.0"
    implementation("androidx.room:room-ktx:$room_version") // Room database with Kotlin extensions
    kapt("androidx.room:room-compiler:$room_version") // Annotation processor for Room (generates database code)

    // Lifecycle components
    val lifecycle_version = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version") // LiveData for reactive UI updates

    // JSON parsing
    implementation("com.google.code.gson:gson:2.8.8") // JSON serialization/deserialization with Gson
    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.9.2") // HTTP client for network requests

}
// Apply Google Services plugin at the bottom (required for Firebase)
apply(plugin = "com.google.gms.google-services")