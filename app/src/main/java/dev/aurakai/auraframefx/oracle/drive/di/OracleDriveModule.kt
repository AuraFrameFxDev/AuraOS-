package dev.aurakai.auraframefx.oracle.drive.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.aurakai.genesis.security.CryptographyManager
import dev.aurakai.genesis.storage.SecureStorage
import dev.aurakai.auraframefx.ai.agents.GenesisAgent
import dev.aurakai.auraframefx.ai.agents.AuraAgent
import dev.aurakai.auraframefx.ai.agents.KaiAgent
import dev.aurakai.auraframefx.oracle.drive.api.OracleDriveApi
import dev.aurakai.auraframefx.oracle.drive.service.GenesisSecureFileService
import dev.aurakai.auraframefx.oracle.drive.service.OracleDriveService
import dev.aurakai.auraframefx.oracle.drive.service.OracleDriveServiceImpl
import dev.aurakai.auraframefx.oracle.drive.service.SecureFileService
import dev.aurakai.auraframefx.security.SecurityContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt Module for Oracle Drive dependencies
 * 
 * Follows Kai's methodology:
 * - Secure by design
 * - Comprehensive dependency management
 * - Clear scoping and lifecycle management
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OracleDriveModule {

    /**
     * Binds OracleDriveServiceImpl as the singleton implementation for OracleDriveService.
     *
     * This Dagger `@Binds` mapping ensures that requests for OracleDriveService
     * receive the OracleDriveServiceImpl instance scoped to the SingletonComponent.
     */
    @Binds
    @Singleton
    abstract fun bindOracleDriveService(
        impl: OracleDriveServiceImpl
    ): OracleDriveService
    
    /**
     * Binds the GenesisSecureFileService implementation to the SecureFileService interface as a singleton.
     *
     * This allows dependency injection of SecureFileService throughout the application using the GenesisSecureFileService implementation.
     */
    @Binds
    @Singleton
    abstract fun bindSecureFileService(
        impl: GenesisSecureFileService
    ): SecureFileService

    companion object {
        /**
         * Provides a singleton OkHttpClient configured for Oracle Drive behavior.
         *
         * The client adds per-request security headers (`X-Security-Token` generated from the provided
         * CryptographyManager and a unique `X-Request-ID`), includes a basic HTTP logging interceptor,
         * and uses 30-second connect/read/write timeouts.
         *
         * @return Configured OkHttpClient instance.
         */
        @Provides
        @Singleton
        fun provideOkHttpClient(
            securityContext: SecurityContext,
            cryptoManager: CryptographyManager
        ): OkHttpClient {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            return OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        // Add security headers
                        .addHeader("X-Security-Token", cryptoManager.generateSecureToken())
                        .addHeader("X-Request-ID", java.util.UUID.randomUUID().toString())
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }

        /**
         * Returns the singleton CryptographyManager initialized with the application context.
         *
         * @return The shared CryptographyManager instance.
         */
        @Provides
        @Singleton
        fun provideGenesisCryptographyManager(
            @ApplicationContext context: Context
        ): CryptographyManager {
            return CryptographyManager.getInstance(context)
        }

        /**
         * Provides the singleton SecureStorage instance initialized with the application context and cryptography manager.
         *
         * @return The singleton SecureStorage.
         */
        @Provides
        @Singleton
        fun provideSecureStorage(
            @ApplicationContext context: Context,
            cryptoManager: CryptographyManager
        ): SecureStorage {
            return SecureStorage.getInstance(context, cryptoManager)
        }

        /**
         * Provides a singleton GenesisSecureFileService configured with the application context, CryptographyManager, and SecureStorage.
         *
         * @return A configured GenesisSecureFileService for performing encrypted/secure file operations.
         */
        @Provides
        @Singleton
        fun provideSecureFileService(
            @ApplicationContext context: Context,
            cryptoManager: CryptographyManager,
            secureStorage: SecureStorage
        ): GenesisSecureFileService {
            return GenesisSecureFileService(context, cryptoManager, secureStorage)
        }

        /**
         * Provides a Retrofit-backed OracleDriveApi using the application's security context for its base URL.
         *
         * Builds a Retrofit instance with base URL = securityContext.getApiBaseUrl() + "/oracle/drive/", the provided OkHttpClient,
         * and a Gson converter, then creates and returns an OracleDriveApi implementation.
         *
         * @return Configured OracleDriveApi.
         */
        @Provides
        @Singleton
        fun provideOracleDriveApi(
            client: OkHttpClient,
            securityContext: SecurityContext
        ): OracleDriveApi {
            return Retrofit.Builder()
                .baseUrl(securityContext.getApiBaseUrl() + "/oracle/drive/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OracleDriveApi::class.java)
        }

        /**
         * Provides a singleton instance of `OracleDriveServiceImpl` configured with the required agents, security context, and Oracle Drive API.
         *
         * @return A singleton `OracleDriveServiceImpl` for Oracle Drive operations.
         */
        @Provides
        @Singleton
        fun provideOracleDriveService(
            genesisAgent: GenesisAgent,
            auraAgent: AuraAgent,
            kaiAgent: KaiAgent,
            securityContext: SecurityContext,
            oracleDriveApi: OracleDriveApi
        ): OracleDriveServiceImpl {
            return OracleDriveServiceImpl(
                genesisAgent = genesisAgent,
                auraAgent = auraAgent,
                kaiAgent = kaiAgent,
                securityContext = securityContext,
                oracleDriveApi = oracleDriveApi
            )
        }
    }
}
