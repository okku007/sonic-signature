import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    // alias(libs.plugins.sqldelight)
}

// sqldelight {
//     databases {
//         create("SonicDatabase") {
//             packageName.set("com.sonicsignature.database")
//         }
//     }
// }

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)

                // Ktor
                api(libs.ktor.client.core)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.client.auth)
                api(libs.ktor.serialization.kotlinx.json)

                // Serialization & Coroutines & DateTime
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)

                // Secure Storage
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)

                // SQLDelight
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.security.crypto)
                implementation(libs.sqldelight.android.driver)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver)
            }
        }

        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
        
        val wasmJsMain by getting {
            dependencies {
                // You can add Wasm Ktor client if network requests are made from shared module
                // For now, Ktor client core is common but wasmJs target might need wasm client engine
            }
        }
    }
}

android {
    namespace = "com.sonicsignature.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
