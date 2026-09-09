import java.util.Properties
import java.io.RandomAccessFile
import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.FilterConfiguration

plugins {
    alias(libs.plugins.agp.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val appMarketingVersion = "27.2"
val appBuildTrain = "1C"
// The 27.1 submission (bda7e69) closed at cumulative build 36.
// Keep the lifetime counter intact; 1C counts reservations after that baseline.
val appBuildSequenceOffset = 36
val androidVersionBase = appMarketingVersion.replace(".", "").toInt()

// versionCode concatenates the marketing base with the build sequence
// zero-padded to three digits (27.2, sequence 2 -> 272002; sequence 10 -> 272010).
// Declared inline at each use site: a top-level fun in this script would force
// ReserveBuildNumber to compile as a non-static inner class and break task creation.

/** One reservation per Gradle invocation, shared by every variant in that build. */
abstract class ReserveBuildNumber : DefaultTask() {
    @get:Input
    abstract val marketingVersion: Property<String>

    @get:Input
    abstract val versionCodeBase: Property<Int>

    @get:Input
    abstract val buildTrain: Property<String>

    @get:Input
    abstract val sequenceOffset: Property<Int>

    @get:Internal
    abstract val counterFile: RegularFileProperty

    @get:OutputFile
    abstract val receiptFile: RegularFileProperty

    init {
        outputs.upToDateWhen { false }
        outputs.doNotCacheIf("Build numbers must never be reused") { true }
    }

    @TaskAction
    fun reserve() {
        val versionBase = versionCodeBase.get()
        val offset = sequenceOffset.get()
        require(offset in 0 until 1_000_000) { "Invalid build sequence baseline" }
        // ASVS 15.4.1/15.4.2: read and increment under the same cross-process lock.
        val next = RandomAccessFile(counterFile.get().asFile, "rw").use { counter ->
            counter.channel.lock().use {
                val previous = if (counter.length() == 0L) offset else {
                    // ASVS 2.2.1: reject a damaged counter instead of resetting it.
                    require(counter.length() <= 16L) { "Invalid build counter" }
                    counter.readLine().trim().toInt()
                }
                require(previous in offset until 1_000_000) {
                    "Build counter precedes the train baseline or exceeds the Android versionCode range"
                }
                val value = previous + 1
                counter.seek(0)
                counter.writeBytes("$value\n")
                counter.setLength(counter.filePointer)
                counter.fd.sync()
                value
            }
        }
        val sequence = next - offset
        receiptFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(sequence.toString())
        }
        logger.lifecycle(
            "Fuyao Locale ${marketingVersion.get()} (${buildTrain.get()}$sequence), " +
                "versionCode=$versionBase${"%03d".format(sequence)}"
        )
    }
}

val reserveBuildNumber = tasks.register<ReserveBuildNumber>("reserveBuildNumber") {
    marketingVersion.set(appMarketingVersion)
    versionCodeBase.set(androidVersionBase)
    buildTrain.set(appBuildTrain)
    sequenceOffset.set(appBuildSequenceOffset)
    counterFile.set(rootProject.layout.projectDirectory.file(".build-counter"))
    receiptFile.set(layout.buildDirectory.file("intermediates/build-number/sequence.txt"))
}
val buildSequence = reserveBuildNumber.map { it.receiptFile.get().asFile.readText().trim().toInt() }

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
        // Static IDE defaults; actual artifacts use the task-backed values below.
        versionCode = "$androidVersionBase${"%03d".format(1)}".toInt()
        versionName = "${appBuildTrain}1"
        buildConfigField("String", "MARKETING_VERSION", "\"$appMarketingVersion\"")
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

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            isUniversalApk = true
        }
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
            // :hidden_api only publishes the built-in build types.
            matchingFallbacks += listOf("debug")
        }

        create("releaseUnsigned") {
            initWith(getByName("release"))
            signingConfig = null
            applicationIdSuffix = ".unsigned"
            versionNameSuffix = "-unsigned"
            manifestPlaceholders["appLabel"] = "Fuyao Locale Release Unsigned"
            matchingFallbacks += listOf("release")
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

androidComponents.onVariants { variant ->
    val apkAppName = rootProject.name
    val marketingVersion = appMarketingVersion
    val variantName = variant.name
    val packageAndBuild = variant.applicationId.zip(buildSequence) { packageName, sequence ->
        packageName to "$appBuildTrain$sequence"
    }
    val suffix = when (variant.buildType) {
        "debug" -> "-debug"
        "debugUnsigned" -> "-debug-unsigned"
        "releaseUnsigned" -> "-unsigned"
        else -> ""
    }
    variant.outputs.forEach { output ->
        output.versionCode.set(buildSequence.map { sequence ->
            "$androidVersionBase${"%03d".format(sequence)}".toInt()
        })
        output.versionName.set(buildSequence.map { "$appBuildTrain$it$suffix" })
        // Public AGP API: the packager and output-metadata.json agree on the
        // filename. Providers keep the build counter out of configuration/help.
        val abi = output.filters.firstOrNull {
            it.filterType == FilterConfiguration.FilterType.ABI
        }?.identifier ?: "universal"
        val extraFilters = output.filters
            .filterNot { it.filterType == FilterConfiguration.FilterType.ABI }
            .joinToString("") { "-${it.filterType.name.lowercase()}-${it.identifier}" }
        output.outputFileName.set(packageAndBuild.map { identity ->
            "${apkAppName}-${identity.first}-${marketingVersion}(${identity.second})" +
                "-${abi}${extraFilters}-${variantName}.apk"
        })
    }
    checkNotNull(variant.buildConfigFields).put("BUILD_NUMBER", buildSequence.map {
        BuildConfigField("String", "\"$appBuildTrain$it\"", "Build train and invocation sequence since baseline")
    })
}

dependencies {
    // JVM behavior tests for the round-8 038/035 logic (see plans/round-8/039)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.json)

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
    implementation(libs.material2)
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

    // Miuix colour system for the Miuix theme
    implementation(libs.miuix.ui)
    // Official miuix glyph set (Search/Sidebar/...) for the Miuix theme chrome
    implementation(libs.miuix.icons)

    // Liquid Glass bottom tab bar: miuix backdrop blur + shader runtime
    // (powers the ported miuix example IosLiquidGlassNavigationBar)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.shader)

    compileOnly(project(":hidden_api"))
}
