plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.habizen"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.habizen"
        minSdk = 29
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    
    // Material Design
    implementation(libs.material)
    
    // Fragment & Navigation
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // WorkManager (for reminders)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Gson (JSON parsing)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // MPAndroidChart (charts)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // ViewPager2 (onboarding)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // CircleImageView (profile photo)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    // Lottie (animations)
    implementation("com.airbnb.android:lottie:6.2.0")
    
    // Calendar view for mood journal
    implementation("com.applandeo:material-calendar-view:1.9.0")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}