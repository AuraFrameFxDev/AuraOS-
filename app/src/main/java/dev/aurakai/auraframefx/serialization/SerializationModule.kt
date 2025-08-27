package dev.aurakai.auraframefx.serialization

import dev.aurakai.auraframefx.system.lockscreen.model.HapticFeedbackConfig
import dev.aurakai.auraframefx.system.lockscreen.model.LockScreenAnimationConfig
import kotlinx.datetime.Instant
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * Comprehensive serialization module for AeGenesis custom serializers
 * Fixes "Serializer has not been found for type 'Any'" errors
 */
val AeGenesisSerializersModule = SerializersModule {
    // Core type serializers
    contextual(Any::class, AnySerializer)
    contextual(Instant::class, InstantSerializer)
    
    // System model serializers
    contextual(HapticFeedbackConfig.serializer())
    contextual(LockScreenAnimationConfig.serializer())
    
    // Add more contextual serializers as needed for other custom types
}

// Alias for backward compatibility  
val AuraFrameSerializersModule = AeGenesisSerializersModule
