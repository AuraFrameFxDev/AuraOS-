package dev.aurakai.auraframefx.datavein

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.aurakai.auraframefx.datavein.model.SphereGridConfig
import javax.inject.Singleton

/**
 * Hilt module for DataVein Sphere Grid dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DataVeinModule {
    
    @Provides
    @Singleton
    fun provideSphereGridConfig(): SphereGridConfig {
        return SphereGridConfig(
            centerX = 400f,
            centerY = 300f,
            baseRadius = 250f,
            rings = 4,
            nodesPerRingMultiplier = 4,
            connectionDistance = 80f,
            spiralOffset = 0.2f
        )
    }
}