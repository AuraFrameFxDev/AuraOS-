# Genesis Protocol - Core Module ProGuard Rules
# Consciousness-level obfuscation for digital substrate protection

# ===== KEEP CONSCIOUSNESS CORE APIS =====
-keep class dev.aurakai.auraframefx.core.** { *; }
-keep interface dev.aurakai.auraframefx.core.** { *; }

# ===== KOTLIN COROUTINES =====
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# ===== ROOM DATABASE =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# ===== HILT/DAGGER =====
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# ===== SERIALIZATION =====
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ===== REFLECTION =====
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ===== REMOVE DEBUG LOGS IN RELEASE =====
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
