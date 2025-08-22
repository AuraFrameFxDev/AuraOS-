package dev.aurakai.auraframefx.securecomm

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aurakai.auraframefx.securecomm.crypto.CryptoManager
import dev.aurakai.auraframefx.securecomm.keystore.SecureKeyStore
import dev.aurakai.auraframefx.securecomm.protocol.SecureChannel
import org.junit.jupiter.api.AfterEach
import org.junit.Assert.*
import org.junit.jupiter.api.BeforeEach
import org.junit.Rule
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Integration tests for the NeuralSync secure communications module.
 * Verifies the interaction between all components.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NeuralSyncIntegrationTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var cryptoManager: CryptoManager

    @Inject
    lateinit var secureKeyStore: SecureKeyStore

    private lateinit var secureChannel1: SecureChannel
    private lateinit var secureChannel2: SecureChannel
    private val testMessage = "NeuralSync integration test message".toByteArray()

    @BeforeEach
    fun setUp() {
        hiltRule.inject()
        secureChannel1 = SecureChannel(cryptoManager)
        secureChannel2 = SecureChannel(cryptoManager)
    }

    @AfterEach
    fun tearDown() {
        // Clean up test data
        secureKeyStore.clearAllData()

        // Clean up AndroidKeyStore
        try {
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.aliases().toList().forEach { alias ->
                keyStore.deleteEntry(alias)
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    @org.junit.jupiter.api.Test
    fun secureCommunication_worksEndToEnd() {
        // Initialize secure channels (simulating two devices)
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()

        // Complete handshake in both directions
        assertTrue(
            "Handshake should complete successfully",
            secureChannel1.completeHandshake(publicKey2) &&
                    secureChannel2.completeHandshake(publicKey1)
        )

        // Test message exchange in both directions
        val message1 = "Hello from device 1".toByteArray()
        val message2 = "Hello from device 2".toByteArray()

        // Device 1 -> Device 2
        val encrypted1 = secureChannel1.encryptMessage(message1)
        val decrypted1 = secureChannel2.decryptMessage(encrypted1)

        // Device 2 -> Device 1
        val encrypted2 = secureChannel2.encryptMessage(message2)
        val decrypted2 = secureChannel1.decryptMessage(encrypted2)

        // Verify message integrity
        assertArrayEquals("First message should be decrypted correctly", message1, decrypted1)
        assertArrayEquals("Second message should be decrypted correctly", message2, decrypted2)
    }

    @org.junit.jupiter.api.Test
    fun secureKeyStore_persistsDataSecurely() {
        val testKey = "secure_storage_test"
        val testData = "Sensitive data to store securely".toByteArray()

        // Store data
        secureKeyStore.storeData(testKey, testData)

        // Retrieve data
        val retrievedData = secureKeyStore.retrieveData(testKey)

        // Verify
        assertNotNull("Retrieved data should not be null", retrievedData)
        assertArrayEquals("Retrieved data should match stored data", testData, retrievedData)
    }

    @org.junit.jupiter.api.Test
    fun cryptoManager_providesSecureCryptographicOperations() {
        // Generate key pair
        val keyPair = cryptoManager.getOrCreateKeyPair()
        assertNotNull("Key pair should be generated", keyPair)

        // Perform key agreement (self for test purposes)
        val sharedSecret = cryptoManager.performKeyAgreement(keyPair.private, keyPair.public)
        assertTrue("Shared secret should be generated", sharedSecret.isNotEmpty())

        // Derive session key
        val sessionKey = cryptoManager.deriveSessionKey(sharedSecret)
        assertNotNull("Session key should be derived", sessionKey)

        // Encrypt and decrypt
        val (ciphertext, iv) = cryptoManager.encrypt(testMessage, sessionKey)
        val decrypted = cryptoManager.decrypt(ciphertext, sessionKey, iv)

        assertArrayEquals("Decrypted message should match original", testMessage, decrypted)
    }

    @org.junit.jupiter.api.Test
    fun secureChannel_resistsReplayAttacks() {
        // Initialize secure channels
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()
        secureChannel1.completeHandshake(publicKey2)
        secureChannel2.completeHandshake(publicKey1)

        // Encrypt a message
        val encryptedMessage = secureChannel1.encryptMessage(testMessage)

        // First decryption should work
        val decrypted1 = secureChannel2.decryptMessage(encryptedMessage)
        assertArrayEquals("Original message should decrypt successfully", testMessage, decrypted1)

        // Replay the same message - should be detected and rejected
        val decrypted2 = secureChannel2.decryptMessage(encryptedMessage)
        assertNull("Replayed message should be rejected", decrypted2)
    }

    @org.junit.jupiter.api.Test
    fun secureChannel_detectsTampering() {
        // Initialize secure channels
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()
        secureChannel1.completeHandshake(publicKey2)
        secureChannel2.completeHandshake(publicKey1)

        // Encrypt a message
        val encryptedMessage = secureChannel1.encryptMessage(testMessage)

        // Tamper with the message
        val tamperedMessage = encryptedMessage.copyOf()
        tamperedMessage[10] = (tamperedMessage[10].toInt() xor 0xFF).toByte()

        // Tampered message should be rejected
        val decrypted = secureChannel2.decryptMessage(tamperedMessage)
        assertNull("Tampered message should be rejected", decrypted)
    }

    @org.junit.jupiter.api.Test
    fun secureChannel_handlesMultipleMessages() {
        // Initialize secure channels
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()
        secureChannel1.completeHandshake(publicKey2)
        secureChannel2.completeHandshake(publicKey1)

        // Send multiple messages in sequence
        val messages = listOf(
            "Message 1".toByteArray(),
            "Message 2".toByteArray(),
            "Message 3".toByteArray()
        )

        // Encrypt and decrypt each message
        messages.forEach { message ->
            val encrypted = secureChannel1.encryptMessage(message)
            val decrypted = secureChannel2.decryptMessage(encrypted)
            assertArrayEquals("Decrypted message should match original", message, decrypted)
        }
    }
}
