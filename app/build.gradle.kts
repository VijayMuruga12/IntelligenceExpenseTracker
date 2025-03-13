plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id ("kotlin-parcelize")

}

android {
    namespace = "com.example.intelligenceexpensetracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.intelligenceexpensetracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation("androidx.core:core:1.13.0")
    implementation("androidx.core:core-ktx:1.13.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")


    // Firebase Bill of Materials
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))

    // Firebase dependencies
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.recyclerview)

//chart dependencies
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
//    implementation ("com.itextpdf:itext7-core:7.2.3")
//    implementation ("com.google.firebase:firebase-database-ktx:20.2.2")

    implementation ("com.google.firebase:firebase-database-ktx:20.2.2")
    implementation ("com.itextpdf:itext7-core:7.2.3")

    implementation ("androidx.cardview:cardview:1.0.0")
    implementation(libs.filament.android)


    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
