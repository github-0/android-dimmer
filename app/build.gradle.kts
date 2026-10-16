plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.dimmer"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.dimmer"
        minSdk = 30
        targetSdk = 37
        versionCode = 4
        versionName = "2026.10.16"
    }

    // Release key credentials come from ~/.gradle/gradle.properties.
    // Without them (e.g. a public checkout) the release build is left unsigned.
    val storeFilePath = providers.gradleProperty("ANDROID_STORE_FILE").orNull
    if (storeFilePath != null) {
        signingConfigs {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = providers.gradleProperty("ANDROID_STORE_PASSWORD").get()
                keyAlias = providers.gradleProperty("ANDROID_KEY_ALIAS").get()
                keyPassword = providers.gradleProperty("ANDROID_KEY_PASSWORD").get()
            }
        }

        buildTypes {
            release {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
