import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// VERSION_NAME in gradle.properties is the single source of truth for the app's
// version: a SemVer string bumped by scripts/release.sh. versionCode is derived from
// it as MAJOR * 10000 + MINOR * 100 + PATCH, so "0.4.0" becomes 400. Minor and patch
// each occupy two decimal digits and must stay within 0-99.
val manualVersionName: String = providers.gradleProperty("VERSION_NAME").get().trim()

val derivedVersionCode: Int = run {
    val segments = manualVersionName.split(".")
    require(segments.size == 3) { "VERSION_NAME '$manualVersionName' must be MAJOR.MINOR.PATCH" }
    val (major, minor, patch) = segments.map {
        it.toIntOrNull() ?: error("VERSION_NAME '$manualVersionName' has a non-numeric segment: '$it'")
    }
    require(minor in 0..99 && patch in 0..99) {
        "VERSION_NAME '$manualVersionName': minor and patch must each be 0-99 " +
            "(they occupy two decimal digits each in versionCode)"
    }
    major * 10000 + minor * 100 + patch
}

// gradle.properties also stores the derived number as a literal VERSION_CODE so the
// tagged commit alone defines the build (CI never computes or passes one). It must
// equal the derived value; the build fails on a mismatch rather than shipping either.
val storedVersionCode: Int = run {
    val raw = providers.gradleProperty("VERSION_CODE").get()
    val stored = raw.trim().toIntOrNull() ?: error("VERSION_CODE '$raw' in gradle.properties is not a number")
    require(stored == derivedVersionCode) {
        "VERSION_CODE=$stored in gradle.properties doesn't match VERSION_NAME $manualVersionName " +
            "(expected $derivedVersionCode). Use scripts/release.sh to bump versions."
    }
    stored
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

android {
    namespace = "com.rrajath.shopp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.rrajath.shopp"
        minSdk = 26
        targetSdk = 36
        versionCode = storedVersionCode
        versionName = manualVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// On CI (GitHub Actions sets CI=true), name APKs {app}-v{versionName}-{buildType}.apk,
// e.g. shopp-v0.2.0-release.apk. Local builds keep the default app-{buildType}.apk.
if (providers.environmentVariable("CI").orNull == "true") {
    val appName = rootProject.name.lowercase()
    androidComponents {
        onVariants { variant ->
            variant.outputs
                .filterIsInstance<com.android.build.api.variant.impl.VariantOutputImpl>()
                .forEach { output ->
                    output.outputFileName.set(
                        output.versionName.map { "$appName-v$it-${variant.buildType}.apk" }
                    )
                }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":designsystem"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.glance.appwidget)
    // Glance 1.1.1 pulls in WorkManager 2.7.1, whose R8 rules let full mode strip
    // OverwritingInputMerger's constructor, leaving release widgets stuck loading.
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
// Print just the versionName (`./gradlew -q :app:printVersionName` -> "0.4.0") so CI
// can check a tag against the source it builds.
tasks.register("printVersionName") {
    doLast { println(manualVersionName) }
}

// Print just the versionCode (`./gradlew -q :app:printVersionCode` -> "400"), already
// checked against VERSION_NAME.
tasks.register("printVersionCode") {
    doLast { println(storedVersionCode) }
}
