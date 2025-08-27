package dev.aurakai.auraframefx.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages authentication tokens securely using EncryptedSharedPreferences.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    private val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "aura_secure_prefs",
        mainKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }

    /**
     * Gets the current access token.
     */
    val accessToken: String?
        get() = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

    /**
     * Gets the current refresh token.
     */
    val refreshToken: String?
        get() = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

    /**
     * Checks if the access token is expired.
     */
    val isTokenExpired: Boolean
        get() {
            val expiry = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0L)
            return System.currentTimeMillis() >= expiry
        }

    /**
     * Updates the stored tokens.
     */
    fun updateTokens(
        accessToken: String,
        refreshToken: String,
        expiresInSeconds: Long
    ) {
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
        
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply()
    }

    /**
     * Clears all stored tokens.
     */
    fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
    }

    /**
     * Checks if user is authenticated (has valid tokens).
     */
    val isAuthenticated: Boolean
        get() = !accessToken.isNullOrBlank() && !isTokenExpired
}
