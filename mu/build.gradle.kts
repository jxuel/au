plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("app.cash.sqldelight") version "2.0.1"
    id("com.google.relay") version "0.3.09"
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")
}

sqldelight {

    databases {

        create("Database") {
            packageName.set("com.el.mu")
            dialect("app.cash.sqldelight:sqlite-3-25-dialect:2.0.1")
        }

    }
}
android {
    namespace = "com.el.mu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.el.mu"
        minSdk = 29
        targetSdk = 33
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
}
// Allow references to generated code
kapt {
    correctErrorTypes = true
}

dependencies {
    implementation("app.cash.sqldelight:android-driver:2.0.1")
    implementation(project(":mservice"))
    implementation(project(":ytrepo"))
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)


    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("com.google.accompanist:accompanist-adaptive:0.33.2-alpha")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.animation:animation:1.5.4")
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //implementation(project(":mservice"))
    // Music
    val media3_version = "1.2.0"

    // For media playback using ExoPlayer
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    // For building media playback UIs
    implementation("androidx.media3:media3-ui:$media3_version")
    // For exposing and controlling media sessions
    implementation("androidx.media3:media3-session:$media3_version")
    implementation("androidx.media3:media3-common:$media3_version")
    // For extracting data from media containers
    implementation("androidx.media3:media3-extractor:$media3_version")
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Nav
    val nav_version = "2.7.6"
    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // Jetpack Compose Integration
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
}