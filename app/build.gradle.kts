import java.util.Properties

plugins {
    alias(libs.plugins.agp.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

private val signingProperties = Properties()
private val signingPropertiesFile = rootProject.file("signing.properties")
private val hasPrivateSigningProperties = signingPropertiesFile.isFile && runCatching {
    signingPropertiesFile.inputStream().use(signingProperties::load)
    listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
        .all { signingProperties.getProperty(it)?.isNotBlank() == true }
}.getOrDefault(false)

android {
    namespace = "ing.fuyaoskyrocket.applocale"
    compileSdk = 37

    defaultConfig {
        applicationId = "ing.fuyaoskyrocket.applocale"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "27.0"
        buildConfigField("String", "BUILD_NUMBER", "\"1A569\"")
        manifestPlaceholders["appLabel"] = "Fuyao Locale"
    }

    val releaseSigningConfig = if (hasPrivateSigningProperties) {
        signingConfigs.create("privateRelease") {
            storeFile = rootProject.file(signingProperties.getProperty("storeFile"))
            storePassword = signingProperties.getProperty("storePassword")
            keyAlias = signingProperties.getProperty("keyAlias")
            keyPassword = signingProperties.getProperty("keyPassword")
        }
    } else {
        // Match Fuyao Color: keep Release locally installable until the private
        // signing properties are present, then prefer the private key automatically.
        signingConfigs.getByName("debug")
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["appLabel"] = "Fuyao Locale Debug"
        }

        getByName("release") {
            // Preserve the publication id and name.
            signingConfig = releaseSigningConfig
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("debugUnsigned") {
            isDebuggable = true
            signingConfig = null
            applicationIdSuffix = ".debug.unsigned"
            versionNameSuffix = "-debug-unsigned"
            manifestPlaceholders["appLabel"] = "Fuyao Locale Debug Unsigned"
        }

        create("releaseUnsigned") {
            initWith(getByName("release"))
            signingConfig = null
            applicationIdSuffix = ".unsigned"
            versionNameSuffix = "-unsigned"
            manifestPlaceholders["appLabel"] = "Fuyao Locale Release Unsigned"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
        compose = true
        aidl = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    debugImplementation(libs.ui.tooling)

    implementation(libs.libsu.core)
    implementation(libs.libsu.service)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)

    // Material 3 Adaptive window information
    implementation(libs.adaptive)

    // Material Components (XML theme parent)
    implementation(libs.material)

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    implementation(libs.hiddenapibypass)

    compileOnly(project(":hidden_api"))
}
