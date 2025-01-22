plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.mservice"
    compileSdk = 34

    defaultConfig {
        minSdk = 29

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
}
kapt {
    correctErrorTypes = true
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation(project(":ytrepo"))
    implementation("androidx.media3:media3-datasource-okhttp:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    val media3_version = "1.2.0"

    // For media playback using ExoPlayer
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    // For building media playback UIs
    implementation("androidx.media3:media3-ui:$media3_version")
    // For exposing and controlling media sessions
    implementation("androidx.media3:media3-session:$media3_version")
    implementation("androidx.media3:media3-common:$media3_version")
    implementation("androidx.datastore:datastore-preferences:1.1.1")


}