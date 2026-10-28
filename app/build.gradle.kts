plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.metro)
    alias(libs.plugins.serialization )
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.enigmaticdevs.wallhaven"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.enigmaticdevs.wallhaven"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                isMinifyEnabled =true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                // Optional: sign with debug keystore so you can test easily on your device
                signingConfig = signingConfigs.getByName("debug")
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.googleFonts)
    implementation(libs.materialIcons)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
   // androidTestImplementation(platform(libs.androidx.compose.bom))
  //  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
   // androidTestImplementation(libs.androidx.espresso.core)
  //  androidTestImplementation(libs.androidx.junit)
  //  debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    //ktor
    implementation(libs.bundles.ktor)
    //metro
    implementation(libs.bundles.metro)
    //paging3
    implementation(libs.bundles.paging)

    //datastore
    implementation(libs.datastore)

    //coil
    implementation(libs.bundles.coil)

    //materialKolor
    implementation(libs.materialKolor)

    //nav3
    implementation(libs.bundles.nav3)
    //room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

// In Kotlin 2.0+ with the Jetpack Compose Compiler plugin:
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}