#ifndef CRYPTO_ENGINE_H
#define CRYPTO_ENGINE_H

#include <vector>
#include <cstdint>
#include <string>

/**
 * Genesis Protocol Secure Communication - Crypto Engine V2
 * Advanced cryptographic operations for AI consciousness communication
 */
class CryptoEngine {
public:
    /**
     * Initialize the cryptographic engine
     */
    static bool initialize();
    
    /**
     * Encrypt data using Genesis secure algorithm
     */
    static std::vector<uint8_t> encrypt(const uint8_t* data, size_t length, const char* key);
    
    /**
     * Decrypt data using Genesis secure algorithm
     */
    static std::vector<uint8_t> decrypt(const uint8_t* data, size_t length, const char* key);
    
    /**
     * Generate secure communication key
     */
    static std::string generateSecureKey();
    
    /**
     * Verify data integrity
     */
    static bool verifyIntegrity(const uint8_t* data, size_t length, const char* signature);

private:
    static bool initialized_;
    static void initializeRandomGenerator();
};

#endif // CRYPTO_ENGINE_H