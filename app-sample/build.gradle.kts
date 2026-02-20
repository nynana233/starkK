plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.cdt.starkk"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.cdt.starkk"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":starkk"))

    // Shared SDK libraries
    implementation(libs.androidx.core.ktx)
    implementation(appSampleLibs.material)

    // App-sample specific libraries
    implementation(appSampleLibs.androidx.lifecycle.runtime.ktx)
    implementation(appSampleLibs.androidx.lifecycle.viewmodel.compose)
    implementation(appSampleLibs.androidx.lifecycle.runtime.compose)
    implementation(appSampleLibs.androidx.activity.compose)
    implementation(platform(appSampleLibs.androidx.compose.bom))
    implementation(appSampleLibs.androidx.compose.ui)
    implementation(appSampleLibs.androidx.compose.ui.graphics)
    implementation(appSampleLibs.androidx.compose.ui.tooling.preview)
    implementation(appSampleLibs.androidx.compose.material3)
    implementation(appSampleLibs.androidx.compose.material.icons.core)
    implementation(appSampleLibs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(appSampleLibs.androidx.junit)

    androidTestImplementation(appSampleLibs.androidx.junit)
    androidTestImplementation(appSampleLibs.androidx.espresso.core)
    androidTestImplementation(platform(appSampleLibs.androidx.compose.bom))
    androidTestImplementation(appSampleLibs.androidx.compose.ui.test.junit4)

    debugImplementation(appSampleLibs.androidx.compose.ui.tooling)
    debugImplementation(appSampleLibs.androidx.compose.ui.test.manifest)
}