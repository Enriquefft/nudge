import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
            storePassword = "NudgeApp2026"
            keyAlias = "nudge"
            keyPassword = "NudgeApp2026"
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            isJniDebuggable = false
            isDebuggable = false
            isShrinkResources = false
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

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}
