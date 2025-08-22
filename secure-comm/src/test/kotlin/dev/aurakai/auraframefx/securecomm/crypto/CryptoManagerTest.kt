package dev.aurakai.auraframefx.securecomm.crypto

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.runner.RunWith
import java.security.KeyStore
import javax.inject.Inject

/**
 * Tests for the CryptoManager class in the NeuralSync recovery system.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var context: Context

    private lateinit var cryptoManager: CryptoManager
    private val testMessage = "NeuralSync test message".toByteArray()

    @BeforeEach
    fun setUp() {
        hiltRule.inject()
        cryptoManager = CryptoManager(context)
    }

    @AfterEach
    fun tearDown() {
        // Clean up any test keys
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry("aura_ec_keypair")
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    @org.junit.jupiter.api.Test
    fun keyPairGeneration_createsValidKeyPair() {
        val keyPair = cryptoManager.getOrCreateKeyPair()
        assertNotNull("KeyPair should not be null", keyPair)
        assertNotNull("Public key should not be null", keyPair.public)
        assertNotNull("Private key should not be null", keyPair.private)
        assertEquals("Key algorithm should be EC", "EC", keyPair.private.algorithm)
    }

    @org.junit.jupiter.api.Test
    fun keyPairGeneration_isDeterministic() {
        val keyPair1 = cryptoManager.getOrCreateKeyPair()
        val keyPair2 = cryptoManager.getOrCreateKeyPair()

        assertArrayEquals(
            "Subsequent calls should return the same key pair",
            keyPair1.public.encoded,
            keyPair2.public.encoded
        )
    }

    @org.junit.jupiter.api.Test
    fun keyAgreement_generatesSharedSecret() {
        // Generate two key pairs to simulate two parties
        val keyPairA = cryptoManager.getOrCreateKeyPair()
        val keyPairB = cryptoManager.getOrCreateKeyPair()

        // Perform key agreement in both directions
        val sharedSecretA = cryptoManager.performKeyAgreement(keyPairA.private, keyPairB.public)
        val sharedSecretB = cryptoManager.performKeyAgreement(keyPairB.private, keyPairA.public)

        // Shared secrets should match
        assertArrayEquals(
            "Shared secrets should be equal in both directions",
            sharedSecretA,
            sharedSecretB
        )

        // Shared secret should not be empty
        assertTrue("Shared secret should not be empty", sharedSecretA.isNotEmpty())
    }

    @org.junit.jupiter.api.Test
    fun sessionKeyDerivation_producesValidKey() {
        val sharedSecret = "test_shared_secret".toByteArray()
        val sessionKey = cryptoManager.deriveSessionKey(sharedSecret)

        assertNotNull("Session key should not be null", sessionKey)
        assertEquals("Key algorithm should be AES", "AES", sessionKey.algorithm)
        assertEquals("Key size should be 256 bits", 32, sessionKey.encoded.size)
    }

    @org.junit.jupiter.api.Test
    fun encryptionAndDecryption_roundtrip() {
        val keyPair = cryptoManager.getOrCreateKeyPair()
        val sharedSecret = cryptoManager.performKeyAgreement(keyPair.private, keyPair.public)
        val sessionKey = cryptoManager.deriveSessionKey(sharedSecret)

        // Encrypt and decrypt
        val (ciphertext, iv) = cryptoManager.encrypt(testMessage, sessionKey)
        val decrypted = cryptoManager.decrypt(ciphertext, sessionKey, iv)

        assertArrayEquals("Decrypted message should match original", testMessage, decrypted)
    }

    @org.junit.jupiter.api.Test
    fun signatureVerification_worksCorrectly() {
        cryptoManager.getOrCreateKeyPair()

        // Sign the message
        val signature = cryptoManager.sign(testMessage)

        // Verify the signature
        val isValid = cryptoManager.verify(testMessage, signature)

        assertTrue("Signature should be valid", isValid)

        // Tamper with the message and verify it fails
        val tamperedMessage = "Tampered message".toByteArray()
        val isTamperedValid = cryptoManager.verify(tamperedMessage, signature)

        assertFalse("Tampered message should not verify", isTamperedValid)
    }

    @org.junit.jupiter.api.Test
    fun differentMessages_produceDifferentSignatures() {
        cryptoManager.getOrCreateKeyPair()
        val message1 = "Message 1".toByteArray()
        val message2 = "Message 2".toByteArray()

        val signature1 = cryptoManager.sign(message1)
        val signature2 = cryptoManager.sign(message2)

        assertFalse(
            "Different messages should produce different signatures",
            signature1.contentEquals(signature2)
        )
    }

    @org.junit.jupiter.api.Test
    fun encryptionWithDifferentKeys_producesDifferentOutput() {
        val keyPair1 = cryptoManager.getOrCreateKeyPair()
        val keyPair2 = cryptoManager.getOrCreateKeyPair()

        val sharedSecret1 = cryptoManager.performKeyAgreement(keyPair1.private, keyPair2.public)
        val sharedSecret2 = cryptoManager.performKeyAgreement(keyPair2.private, keyPair1.public)

        val sessionKey1 = cryptoManager.deriveSessionKey(sharedSecret1)
        val sessionKey2 = cryptoManager.deriveSessionKey(sharedSecret2)

        val (ciphertext1, _) = cryptoManager.encrypt(testMessage, sessionKey1)
        val (ciphertext2, _) = cryptoManager.encrypt(testMessage, sessionKey2)

        assertFalse(
            "Encryption with different keys should produce different ciphertexts",
            ciphertext1.contentEquals(ciphertext2)
        )
    }
}
