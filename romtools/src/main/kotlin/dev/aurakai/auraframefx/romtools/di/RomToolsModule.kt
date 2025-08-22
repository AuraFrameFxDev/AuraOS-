// File: romtools/src/main/kotlin/dev/aurakai/auraframefx/romtools/di/RomToolsModule.kt
package dev.aurakai.auraframefx.romtools.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.aurakai.auraframefx.romtools.BackupManager
import dev.aurakai.auraframefx.romtools.BackupManagerImpl
import dev.aurakai.auraframefx.romtools.FlashManager
import dev.aurakai.auraframefx.romtools.FlashManagerImpl
import dev.aurakai.auraframefx.romtools.RecoveryManager
import dev.aurakai.auraframefx.romtools.RecoveryManagerImpl
import dev.aurakai.auraframefx.romtools.RomVerificationManager
import dev.aurakai.auraframefx.romtools.RomVerificationManagerImpl
import dev.aurakai.auraframefx.romtools.SystemModificationManager
import dev.aurakai.auraframefx.romtools.SystemModificationManagerImpl
import dev.aurakai.auraframefx.romtools.bootloader.BootloaderManager
import dev.aurakai.auraframefx.romtools.bootloader.BootloaderManagerImpl
import javax.inject.Singleton

/**
 * Hilt module providing dependencies for the ROM Tools system.
 * Integrates all ROM modification and management components.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RomToolsModule {

    @Binds
    @Singleton
    abstract fun bindBootloaderManager(
        bootloaderManagerImpl: BootloaderManagerImpl
    ): BootloaderManager

    @Binds
    @Singleton
    abstract fun bindRecoveryManager(
        recoveryManagerImpl: RecoveryManagerImpl
    ): RecoveryManager

    @Binds
    @Singleton
    abstract fun bindSystemModificationManager(
        systemModificationManagerImpl: SystemModificationManagerImpl
    ): SystemModificationManager

    @Binds
    @Singleton
    abstract fun bindFlashManager(
        flashManagerImpl: FlashManagerImpl
    ): FlashManager

    @Binds
    @Singleton
    abstract fun bindRomVerificationManager(
        romVerificationManagerImpl: RomVerificationManagerImpl
    ): RomVerificationManager

    @Binds
    @Singleton
    abstract fun bindBackupManager(
        backupManagerImpl: BackupManagerImpl
    ): BackupManager

    companion object {

        @Provides
        @RomToolsDataDir
        fun provideRomToolsDataDirectory(
            @ApplicationContext context: Context
        ): String {
            return "${context.filesDir}/romtools"
        }

        @Provides
        @RomToolsBackupDir
        fun provideRomToolsBackupDirectory(
            @ApplicationContext context: Context
        ): String {
            return "${context.getExternalFilesDir(null)}/backups"
        }

        @Provides
        @RomToolsDownloadDir
        fun provideRomToolsDownloadDirectory(
            @ApplicationContext context: Context
        ): String {
            return "${context.getExternalFilesDir(null)}/downloads"
        }

        @Provides
        @RomToolsTempDir
        fun provideRomToolsTempDirectory(
            @ApplicationContext context: Context
        ): String {
            return "${context.cacheDir}/romtools_temp"
        }
    }
}

// Qualifier annotations for ROM tools directories
@Retention(AnnotationRetention.BINARY)
annotation class RomToolsDataDir

@Retention(AnnotationRetention.BINARY)
annotation class RomToolsBackupDir

@Retention(AnnotationRetention.BINARY)
annotation class RomToolsDownloadDir

@Retention(AnnotationRetention.BINARY)
annotation class RomToolsTempDir
