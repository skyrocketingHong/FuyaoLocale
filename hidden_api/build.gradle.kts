plugins {
    alias(libs.plugins.agp.library)
}

android {
    namespace = "ing.fuyaoskyrocket.applocale.hiddenapi"
    compileSdk = 37

    defaultConfig {
        minSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {}
