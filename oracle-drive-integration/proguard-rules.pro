# ProGuard rules for Oracle Drive Integration module

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# Keep Oracle Drive classes
-keep class dev.aurakai.auraframefx.oracle.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
