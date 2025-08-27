# AeGenesis Module Architecture Summary

## JVM-Only Modules (Pure Kotlin):
- **core-module**: Shared utilities, no Android APIs
- **romtools**: ROM analysis tools, no Android APIs

## Android Library Modules:
- **secure-comm**: Security and crypto (uses Android Keystore, Context, etc.)
- **oracle-drive-integration**: Oracle Drive integration with Android APIs
- **collab-canvas**: Collaborative canvas with Compose UI

## Main Android Application:
- **app**: Main application module

## Key Points:
- All modules now use **Java 21** consistently
- **secure-comm** converted back to Android library (it needs Android APIs)
- **KSP2 disabled** (`ksp.useKSP2=false`) to prevent NullPointerException
- **OpenAPI validation disabled** to prevent spec validation errors
- **JVM target consistency** fixed across all modules
