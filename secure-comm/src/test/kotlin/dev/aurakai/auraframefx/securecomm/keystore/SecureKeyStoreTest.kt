package dev.aurakai.auraframefx.securecomm.keystore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import java.security.KeyStore

/**
 * Tests for the SecureKeyStore class in the NeuralSync recovery system.
 */
@RunWith(AndroidJUnit4::class)
class SecureKeyStoreTest {
    private lateinit var secureKeyStore: SecureKeyStore
    private lateinit var context: Context
    private val testKey = "test_key"
    private val testData = "NeuralSync test data".toByteArray()

    @BeforeEach
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        secureKeyStore = SecureKeyStore(context)

        // Clear any existing test data
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    @AfterEach
    fun tearDown() {
        // Clean up test keys from AndroidKeyStore
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Delete all test keys
            keyStore.aliases().toList().forEach { alias ->
                if (alias.startsWith("aura_secure_key_")) {
                    keyStore.deleteEntry(alias)
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    @org.junit.jupiter.api.Test
    fun storeAndRetrieveData_worksCorrectly() {
        // Store data
        secureKeyStore.storeData(testKey, testData)

        // Retrieve data
        val retrievedData = secureKeyStore.retrieveData(testKey)

        assertNotNull("Retrieved data should not be null", retrievedData)
        assertArrayEquals("Retrieved data should match stored data", testData, retrievedData)
    }

    @Test
    fun retrieveNonExistentKey_returnsNull() {
        val retrievedData = secureKeyStore.retrieveData("non_existent_key")
        assertNull("Retrieving non-existent key should return null", retrievedData)
    }

    @Test
    fun overwriteData_worksCorrectly() {
        val initialData = "initial data".toByteArray()
        val updatedData = "updated data".toByteArray()

        // Store initial data
        secureKeyStore.storeData(testKey, initialData)

        // Overwrite with updated data
        secureKeyStore.storeData(testKey, updatedData)

        // Retrieve and verify
        val retrievedData = secureKeyStore.retrieveData(testKey)
        assertArrayEquals("Retrieved data should be the updated data", updatedData, retrievedData)
    }

    @org.junit.jupiter.api.Test
    fun removeData_worksCorrectly() {
        // Store data
        secureKeyStore.storeData(testKey, testData)

        // Remove data
        secureKeyStore.removeData(testKey)

        // Verify removal
        val retrievedData = secureKeyStore.retrieveData(testKey)
        assertNull("Data should be removed", retrievedData)
    }

    @Test
    fun clearAllData_worksCorrectly() {
        // Store multiple data items
        secureKeyStore.storeData("key1", "data1".toByteArray())
        secureKeyStore.storeData("key2", "data2".toByteArray())
        secureKeyStore.storeData("key3", "data3".toByteArray())

        // Clear all data
        secureKeyStore.clearAllData()

        // Verify all data is cleared
        assertNull(secureKeyStore.retrieveData("key1"))
        assertNull(secureKeyStore.retrieveData("key2"))
        assertNull(secureKeyStore.retrieveData("key3"))
    }

    @Test
    fun encryptionAndDecryption_roundtrip() {
        val testKey = "encryption_test_key"
        val testMessage = "This is a test message for encryption".toByteArray()

        // Store encrypted data
        secureKeyStore.storeData(testKey, testMessage)

        // Retrieve and decrypt data
        val decrypted = secureKeyStore.retrieveData(testKey)

        assertArrayEquals("Decrypted data should match original", testMessage, decrypted)
    }

    @org.junit.jupiter.api.Test
    fun differentKeys_produceDifferentCiphertexts() {
        val message = "Same message, different keys".toByteArray()
        val key1 = "key1"
        val key2 = "key2"

        // Store same message with different keys
        secureKeyStore.storeData(key1, message)
        secureKeyStore.storeData(key2, message)

        // Get the raw encrypted values
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val encrypted1 = prefs.getString(key1, null)
        val encrypted2 = prefs.getString(key2, null)

        assertNotNull("First encrypted value should not be null", encrypted1)
        assertNotNull("Second encrypted value should not be null", encrypted2)
        assertNotEquals(
            "Same message with different keys should produce different ciphertexts",
            encrypted1,
            encrypted2
        )
    }

    @Test
    fun tamperedCiphertext_failsDecryption() {
        // Store data
        secureKeyStore.storeData(testKey, testData)

        // Get the raw encrypted value
        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val encrypted = prefs.getString(testKey, null) ?: fail("Encrypted data should not be null")

        // Tamper with the stored value (flip a bit in the base64 string)
        val tampered = encrypted.toCharArray().apply {
            this[10] = if (this[10] == 'A') 'B' else 'A'
        }.concatToString()

        // Save the tampered value
        prefs.edit().putString(testKey, tampered).apply()

        // Attempt to retrieve - should fail to decrypt
        val retrieved = secureKeyStore.retrieveData(testKey)
        assertNull("Tampered ciphertext should fail decryption", retrieved)
    }

    @Test
    fun keyRotation_worksCorrectly() {
        // Store data with initial key
        secureKeyStore.storeData(testKey, testData)

        // Get the key alias
        val keyAlias = "${SecureKeyStore.KEY_ALIAS}_$testKey"

        // Delete the key to simulate key rotation
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        keyStore.deleteEntry(keyAlias)

        // Store data again - should create a new key
        secureKeyStore.storeData(testKey, testData)

        // Verify we can still retrieve the data
        val retrieved = secureKeyStore.retrieveData(testKey)
        assertArrayEquals("Data should still be accessible after key rotation", testData, retrieved)
    }
}
