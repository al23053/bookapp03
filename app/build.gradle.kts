plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bookapp03"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bookapp03"
        minSdk = 21
        targetSdk = 35
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
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
        }
    }

    dependencies {
        implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
        implementation("com.google.firebase:firebase-analytics")

        // Firestore
        implementation("com.google.firebase:firebase-firestore-ktx")
        // Firebase Authentication
        implementation("com.google.firebase:firebase-auth-ktx")
        implementation(libs.appcompat)
        implementation(libs.material)

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)

        // Room components
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")
        // Core Android dependencies
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")

        //Google Books API
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("com.android.volley:volley:1.2.1")
        implementation("com.squareup.okhttp3:okhttp:4.11.0")
        // implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

        // JUnit
        testImplementation("junit:junit:4.13.2")
        // Mockito
        testImplementation("org.mockito:mockito-core:4.+")
        // Robolectric (RobolectricTestRunner を使っている場合)
        testImplementation("org.robolectric:robolectric:4.10.3")
        // AndroidX Test Core（Robolectric 用）
        testImplementation("androidx.test:core:1.5.0")
        testImplementation("androidx.test.ext:junit:1.1.3")
        testImplementation("androidx.test:runner:1.5.2")
        testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    }
}


