import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.aleph.nudge"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file("${rootProject.projectDir}/nudge-release.jks")
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD", "")
            keyAlias = "nudge"
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD", "")
        }
    }

    defaultConfig {
        applicationId = "com.aleph.nudge"
        minSdk = 21
        targetSdk = 29
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        buildConfigField("String", "ZAI_API_KEY", "\"${localProperties.getProperty("ZAI_API_KEY", "")}\"")
        buildConfigField("String", "ZAI_BASE_URL", "\"https://api.z.ai/api/paas/v4\"")
        buildConfigField("String", "ZAI_MODEL", "\"glm-4.7-flash\"")
        buildConfigField("String", "SENTRY_DSN", "\"${localProperties.getProperty("SENTRY_DSN", "")}\"")
        buildConfigField("String", "BACKEND_URL", "\"${localProperties.getProperty("BACKEND_URL", "https://nudge-api.fly.dev")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            isJniDebuggable = false
            isDebuggable = false
        }
    }

    flavorDimensions += "mode"

    productFlavors {
        create("clover") {
            dimension = "mode"
            buildConfigField("Boolean", "IS_DEMO", "false")
            buildConfigField("Boolean", "IS_CLOVER_BUILD", "true")
        }
        create("pilot") {
            dimension = "mode"
            applicationIdSuffix = ".pilot"
            buildConfigField("Boolean", "IS_DEMO", "false")
            buildConfigField("Boolean", "IS_CLOVER_BUILD", "false")
        }
        create("demo") {
            dimension = "mode"
            applicationIdSuffix = ".demo"
            buildConfigField("Boolean", "IS_DEMO", "true")
            buildConfigField("Boolean", "IS_CLOVER_BUILD", "false")
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
        buildConfig = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation("com.clover.sdk:clover-android-sdk:329")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    implementation("androidx.multidex:multidex:2.0.1")

    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("io.sentry:sentry-android:7.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}
