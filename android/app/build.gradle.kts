import java.io.FileInputStream
import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("de.mannodermaus.android-junit5") version "1.14.0.0"
}

val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

val keyFilePath = System.getenv("LITTLE_CARD_KEY_FILE") ?: "key.properties"
val keyPropsFile = rootProject.file(keyFilePath)
val keyProperties = Properties()
if (keyPropsFile.exists()) {
    keyProperties.load(FileInputStream(keyPropsFile))
} else {
    logger.warn("Key properties file not found at $keyFilePath, proceeding without it.")
}

android {
    signingConfigs {
        create("release") {
            val storePathProperty = keyProperties.getProperty("KEYSTORE_PATH")
            val storePasswordProperty = keyProperties.getProperty("KEYSTORE_PASSWORD")
            val keyAliasProperty = keyProperties.getProperty("KEY_ALIAS")
            val keyPasswordProperty = keyProperties.getProperty("KEY_PASSWORD")

            val missing = listOf(
                "KEYSTORE_PATH" to storePathProperty,
                "KEYSTORE_PASSWORD" to storePasswordProperty,
                "KEY_ALIAS" to keyAliasProperty,
                "KEY_PASSWORD" to keyPasswordProperty
            ).filter { it.second.isNullOrBlank() }.map { it.first }

            if (missing.isEmpty()) {
                storeFile = rootProject.file(storePathProperty!!)
                storePassword = storePasswordProperty!!
                keyAlias = keyAliasProperty!!
                keyPassword = keyPasswordProperty!!
            } else if (isReleaseBuild) {
                throw GradleException(
                    "Missing signing properties: ${missing.joinToString(", ")} in $keyFilePath. Provide them in `key.properties` or set `LITTLE_CARD_KEY_FILE` env."
                )
            }
        }
    }
    namespace = "hu.sarmin.yt2ig"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "hu.sarmin.yt2ig"
        minSdk = 27
        targetSdk = 36
        versionCode = 3
        versionName = "mvp-rc2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val youtubeApiKey = keyProperties.getProperty("YOUTUBE_API_KEY")
        if (youtubeApiKey.isNullOrBlank() && isReleaseBuild) {
            throw GradleException("YOUTUBE_API_KEY is not defined in $keyFilePath.")
        }

        buildConfigField(
            "String",
            "YOUTUBE_API_KEY",
            "\"$youtubeApiKey\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.okhttp)
    implementation(libs.androidx.palette.ktx)
    implementation("androidx.compose.material:material-icons-extended:1.7.7")
    implementation(libs.androidx.browser)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
    testImplementation("io.mockk:mockk-android:1.14.6")
    testImplementation(libs.mockwebserver3)
    testImplementation(libs.json) // android's own implementation is not available in tests
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
