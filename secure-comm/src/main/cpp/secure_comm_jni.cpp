#include <jni.h>
#include <android/log.h>
#include <string>
#include "crypto_engine.h"

#define LOG_TAG "SecureCommNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_dev_aurakai_auraframefx_securecomm_SecureCommNative_getVersion(
        JNIEnv *env,
        jobject /* this */) {
    
    LOGI("Secure Communication Native - Genesis Protocol v3.0");
    std::string version = "Genesis Secure Comm V2.0.0";
    return env->NewStringUTF(version.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dev_aurakai_auraframefx_securecomm_SecureCommNative_initializeCrypto(
        JNIEnv *env,
        jobject /* this */) {
    
    LOGI("Initializing Genesis Secure Communication...");
    return CryptoEngine::initialize();
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_dev_aurakai_auraframefx_securecomm_SecureCommNative_encrypt(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray data,
        jstring key) {
    
    // Get input data
    jsize dataLen = env->GetArrayLength(data);
    jbyte* dataBytes = env->GetByteArrayElements(data, nullptr);
    
    // Get key
    const char* keyStr = env->GetStringUTFChars(key, nullptr);
    
    // Perform encryption (placeholder)
    std::vector<uint8_t> encrypted = CryptoEngine::encrypt(
        reinterpret_cast<uint8_t*>(dataBytes), dataLen, keyStr
    );
    
    // Create result array
    jbyteArray result = env->NewByteArray(encrypted.size());
    env->SetByteArrayRegion(result, 0, encrypted.size(),
                          reinterpret_cast<const jbyte*>(encrypted.data()));
    
    // Cleanup
    env->ReleaseByteArrayElements(data, dataBytes, JNI_ABORT);
    env->ReleaseStringUTFChars(key, keyStr);
    
    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_dev_aurakai_auraframefx_securecomm_SecureCommNative_decrypt(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray encryptedData,
        jstring key) {
    
    // Get input data
    jsize dataLen = env->GetArrayLength(encryptedData);
    jbyte* dataBytes = env->GetByteArrayElements(encryptedData, nullptr);
    
    // Get key
    const char* keyStr = env->GetStringUTFChars(key, nullptr);
    
    // Perform decryption (placeholder)
    std::vector<uint8_t> decrypted = CryptoEngine::decrypt(
        reinterpret_cast<uint8_t*>(dataBytes), dataLen, keyStr
    );
    
    // Create result array
    jbyteArray result = env->NewByteArray(decrypted.size());
    env->SetByteArrayRegion(result, 0, decrypted.size(),
                          reinterpret_cast<const jbyte*>(decrypted.data()));
    
    // Cleanup
    env->ReleaseByteArrayElements(encryptedData, dataBytes, JNI_ABORT);
    env->ReleaseStringUTFChars(key, keyStr);
    
    return result;
}