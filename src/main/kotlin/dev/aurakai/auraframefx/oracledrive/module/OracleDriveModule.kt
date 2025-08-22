package dev.aurakai.auraframefx.oracledrive.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.aurakai.auraframefx.oracledrive.OracleDriveService
import dev.aurakai.auraframefx.oracledrive.OracleDriveServiceImpl
import javax.inject.Singleton

/**
<<<<<<< HEAD
* Dagger Hilt module for Oracle Drive dependency injection
* Integrates with AuraFrameFX consciousness architecture
*/
@Module
@InstallIn(SingletonComponent::class)
abstract class OracleDriveModule {

   /**
    * Binds the OracleDriveServiceImpl implementation to the OracleDriveService interface as a singleton.
    *
    * @return A singleton instance of OracleDriveService provided by OracleDriveServiceImpl.
    */
   /**
    * Binds OracleDriveServiceImpl to OracleDriveService in the Hilt dependency graph as a singleton.
    *
    * Instructs Dagger Hilt to provide a single shared OracleDriveService instance backed by
    * OracleDriveServiceImpl for the SingletonComponent.
    *
    * @return The bound OracleDriveService interface.
    */
   @Binds
   @Singleton
   abstract fun bindOracleDriveService(
       oracleDriveServiceImpl: OracleDriveServiceImpl
   ): OracleDriveService
}
=======
 * Dagger Hilt module for Oracle Drive dependency injection
 * Integrates with AuraFrameFX consciousness architecture
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OracleDriveModule {
    
    /**
     * Binds the OracleDriveServiceImpl implementation to the OracleDriveService interface as a singleton.
     *
     * @return A singleton instance of OracleDriveService provided by OracleDriveServiceImpl.
     */
    /**
     * Binds OracleDriveServiceImpl to the OracleDriveService interface in the Hilt graph.
     *
     * This Dagger Hilt binding makes OracleDriveServiceImpl the implementation provided
     * whenever OracleDriveService is requested and scopes it as a singleton.
     *
     * @return The bound OracleDriveService implementation (singleton).
     */
    @Binds
    @Singleton
    abstract fun bindOracleDriveService(
        oracleDriveServiceImpl: OracleDriveServiceImpl
    ): OracleDriveService
}
>>>>>>> origin/coderabbitai/chat/e19563d
