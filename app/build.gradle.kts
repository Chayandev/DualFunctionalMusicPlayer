plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

}

android {
    namespace = "com.example.cdmusicplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cdmusicplayer"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding =true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.7.6")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    //gson converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //picasso to conver image link to image
    implementation("com.squareup.picasso:picasso:2.8")

    //lotie animation
    implementation("com.airbnb.android:lottie:3.4.0")

    //custom seek bar
    implementation ("me.tankery.lib:circularSeekBar:1.4.2")

    //notification
    implementation("androidx.media:media:1.7.0")

    //Lifecycle Extension
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    //viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    //LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    //
    //noinspection LifecycleAnnotationProcessorWithJava8
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.7.0")

    //pallet library
    implementation ("androidx.palette:palette-ktx:1.0.0")

    //navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.7.6")


    //glide image laoder
    implementation ("com.github.bumptech.glide:glide:4.16.0")

}