import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

extensions.configure<ApplicationExtension>("android") {
    namespace = "com.sonicsignature.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sonicsignature.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    // Signing configuration — credentials are injected via environment variables in CI.
    // When building locally without these env vars set, the signing config is defined
    // but NOT assigned to the release build type, so local builds still work.
    val keystorePath   = System.getenv("KEYSTORE_PATH")
    val keyAlias       = System.getenv("KEY_ALIAS")
    val storePassword  = System.getenv("STORE_PASSWORD")
    val keyPassword    = System.getenv("KEY_PASSWORD")
    val hasSigningConfig = keystorePath != null && keyAlias != null &&
                           storePassword != null && keyPassword != null

    if (hasSigningConfig) {
        signingConfigs {
            create("release") {
                storeFile          = file(keystorePath!!)
                this.keyAlias      = keyAlias
                this.storePassword = storePassword
                this.keyPassword   = keyPassword
            }
        }
    }

    buildTypes {
        release {
            // Minification + shrinking for production builds
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only assign signing config when CI credentials are available.
            // Omitting it locally produces an unsigned APK (expected for dev builds)
            // rather than a broken-config crash.
            if (hasSigningConfig) {
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
    }

    packaging {
        jniLibs {
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Ktor (for HttpClient setup in AppNavGraph)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
