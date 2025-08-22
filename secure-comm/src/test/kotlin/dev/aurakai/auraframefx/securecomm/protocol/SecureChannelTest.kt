package dev.aurakai.auraframefx.securecomm.protocol

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.aurakai.auraframefx.securecomm.crypto.CryptoManager
import org.junit.Assert.*
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Tests for the SecureChannel class in the NeuralSync recovery system.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SecureChannelTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var context: Context

    private lateinit var cryptoManager: CryptoManager
    private lateinit var secureChannel1: SecureChannel
    private lateinit var secureChannel2: SecureChannel
    private val testMessage = "NeuralSync secure channel test message".toByteArray()

    @BeforeEach
    fun setUp() {
        hiltRule.inject()
        cryptoManager = CryptoManager(context)
        secureChannel1 = SecureChannel(cryptoManager)
        secureChannel2 = SecureChannel(cryptoManager)
    }

    @AfterEach
    fun tearDown() {
        // Clean up any test keys
        try {
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry("aura_ec_keypair")
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    @Test
    fun handshake_establishesSecureChannel() {
        // Initialize both channels
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()

        // Complete handshake in both directions
        val handshake1 = secureChannel1.completeHandshake(publicKey2)
        val handshake2 = secureChannel2.completeHandshake(publicKey1)

        assertTrue("Handshake should complete successfully", handshake1 && handshake2)
        assertTrue("Secure channel 1 should be ready", secureChannel1.isReady())
        assertTrue("Secure channel 2 should be ready", secureChannel2.isReady())
    }

    @org.junit.jupiter.api.Test
    fun secureMessageExchange_worksCorrectly() {
        // Set up secure channel
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()
        secureChannel1.completeHandshake(publicKey2)
        secureChannel2.completeHandshake(publicKey1)

        // Encrypt message from channel 1 to channel 2
        val encryptedMessage = secureChannel1.encryptMessage(testMessage)

        // Decrypt message in channel 2
        val decryptedMessage = secureChannel2.decryptMessage(encryptedMessage)

        assertNotNull("Decrypted message should not be null", decryptedMessage)
        assertArrayEquals("Decrypted message should match original", testMessage, decryptedMessage)
    }

    @org.junit.jupiter.api.Test
    fun bidirectionalCommunication_works() {
        // Set up secure channel
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()
        secureChannel1.completeHandshake(publicKey2)
        secureChannel2.completeHandshake(publicKey1)

        // Channel 1 sends to Channel 2
        val message1 = "Message from channel 1".toByteArray()
        val encrypted1 = secureChannel1.encryptMessage(message1)
        val decrypted1 = secureChannel2.decryptMessage(encrypted1)

        // Channel 2 sends to Channel 1
        val message2 = "Response from channel 2".toByteArray()
        val encrypted2 = secureChannel2.encryptMessage(message2)
        val decrypted2 = secureChannel1.decryptMessage(encrypted2)

        assertArrayEquals("First message should be decrypted correctly", message1, decrypted1)
        assertArrayEquals("Second message should be decrypted correctly", message2, decrypted2)
    }

    @org.junit.jupiter.api.Test
    fun tamperedMessage_detection() {
        // Set up secure channel
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()
        secureChannel1.completeHandshake(publicKey2)
        secureChannel2.completeHandshake(publicKey1)

        // Encrypt a message
        val encryptedMessage = secureChannel1.encryptMessage(testMessage)

        // Tamper with the message (flip a bit in the ciphertext)
        val tamperedMessage = encryptedMessage.copyOf()
        tamperedMessage[10] = (tamperedMessage[10].toInt() xor 0xFF).toByte()

        // Attempt to decrypt the tampered message
        val decrypted = secureChannel2.decryptMessage(tamperedMessage)

        assertNull("Tampered message should not decrypt successfully", decrypted)
    }

    @Test
    fun replayAttack_prevention() {
        // Set up secure channel
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()
        secureChannel1.completeHandshake(publicKey2)
        secureChannel2.completeHandshake(publicKey1)

        // Encrypt a message
        val encryptedMessage1 = secureChannel1.encryptMessage(testMessage)

        // Replay the same message
        val encryptedMessage2 = encryptedMessage1.copyOf()

        // First decryption should work
        val decrypted1 = secureChannel2.decryptMessage(encryptedMessage1)
        assertNotNull("Original message should decrypt successfully", decrypted1)

        // Second decryption should fail (replay attack)
        val decrypted2 = secureChannel2.decryptMessage(encryptedMessage2)
        assertNull("Replayed message should not decrypt successfully", decrypted2)
    }

    @Test
    fun reset_clearsSessionState() {
        // Set up secure channel
        val publicKey1 = secureChannel1.initialize()
        val publicKey2 = secureChannel2.initialize()
        secureChannel1.completeHandshake(publicKey2)
        secureChannel2.completeHandshake(publicKey1)

        // Verify channel is ready
        assertTrue(secureChannel1.isReady())

        // Reset the channel
        secureChannel1.reset()

        // Verify channel is no longer ready
        assertFalse("Channel should not be ready after reset", secureChannel1.isReady())

        // Attempting to encrypt should throw
        try {
            secureChannel1.encryptMessage(testMessage)
            fail("Encrypting after reset should throw IllegalStateException")
        } catch (e: IllegalStateException) {
            // Expected
        }
    }

    @org.junit.jupiter.api.Test
    fun messagePacket_serializationRoundtrip() {
        val original = MessagePacket(
            ciphertext = "test ciphertext".toByteArray(),
            iv = "test iv".toByteArray(),
            signature = "test signature".toByteArray()
        )

        val serialized = original.toByteArray()
        val deserialized = MessagePacket.fromByteArray(serialized)

        assertEquals(
            "Deserialized packet should match original",
            original,
            deserialized
        )
    }
}
