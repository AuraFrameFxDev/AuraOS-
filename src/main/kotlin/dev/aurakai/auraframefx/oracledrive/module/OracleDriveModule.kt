package dev.aurakai.auraframefx.oracledrive.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.aurakai.auraframefx.oracledrive.OracleDriveService
import dev.aurakai.auraframefx.oracledrive.OracleDriveServiceImpl
import javax.inject.Singleton

/**
 * Dagger Hilt module for Oracle Drive library components
 * 
 * Provides dependency injection bindings for Oracle Drive services,
 * APIs, and supporting infrastructure components.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OracleDriveModule {
    
    /**
     * Binds the Oracle Drive service implementation
     * 
     * @param impl The implementation to bind
     * @return The service interface
     */
    @Binds
    @Singleton
    abstract fun bindOracleDriveService(
        impl: OracleDriveServiceImpl
    ): OracleDriveService
}
