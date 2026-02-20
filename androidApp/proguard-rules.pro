# Add project-specific ProGuard rules here.
# By default, the flags from the Android Gradle Plugin's built-in
# proguard-android-optimize.txt are applied.

# ── Error Prone annotations (referenced by Google Tink, not shipped in APK) ──
# R8 treats missing referenced classes as errors — suppress them.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# ── Google Tink ───────────────────────────────────────────────────────────────
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ── Kotlinx Serialization ─────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# ── Ktor ──────────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ── OkHttp ────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Compose ───────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── App data models (shared module) ──────────────────────────────────────────
# Keep all model classes so serialization / reflection works correctly.
-keep class com.sonicsignature.model.** { *; }
-keep class com.sonicsignature.storage.** { *; }
