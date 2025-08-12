# Genesis-OS Secure Communication ProGuard Rules

# Keep secure communication classes
-keep class dev.aurakai.auraframefx.secure.** { *; }

# Keep encryption related classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }