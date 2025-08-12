# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Genesis-OS Auto-Provisioned ProGuard Rules

# Keep Genesis Core classes
-keep class dev.aurakai.auraframefx.core.** { *; }

# Keep Hilt components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }