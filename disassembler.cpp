#include <jni.h>
#include <string>
#include <vector>
#include <cstring>

// Note: This is a template for future Capstone integration
// Capstone library would be added as a dependency via CMake

extern "C" {

/**
 * JNI function to disassemble ARM64 code
 * This is a placeholder that returns mock data
 * In production, integrate with Capstone library
 *
 * @param env JNI environment
 * @param clazz Class reference
 * @param code Byte array containing machine code
 * @param address Starting address for disassembly
 * @return Array of disassembled instructions as strings
 */
JNIEXPORT jobjectArray JNICALL
Java_com_example_soanalyzer_utils_NativeDisassembler_disassembleARM64(
        JNIEnv *env,
        jclass clazz,
        jbyteArray code,
        jlong address) {

    // Get code bytes
    jbyte *codeBytes = env->GetByteArrayElements(code, nullptr);
    jsize codeLength = env->GetArrayLength(code);

    // TODO: Integrate Capstone here
    // cs_insn *insn;
    // size_t count = cs_disasm(handle, (uint8_t*)codeBytes, codeLength, address, 0, &insn);

    // For now, return empty array (placeholder)
    jobjectArray result = env->NewObjectArray(0, env->FindClass("java/lang/String"), nullptr);

    env->ReleaseByteArrayElements(code, codeBytes, JNI_ABORT);
    return result;
}

/**
 * JNI function to parse ELF header
 * Extracts basic ELF information
 *
 * @param env JNI environment
 * @param clazz Class reference
 * @param data Byte array containing ELF file
 * @return Array with [magic, class, data, version, osabi]
 */
JNIEXPORT jintArray JNICALL
Java_com_example_soanalyzer_utils_NativeDisassembler_parseELFHeader(
        JNIEnv *env,
        jclass clazz,
        jbyteArray data) {

    jbyte *bytes = env->GetByteArrayElements(data, nullptr);
    jsize dataLength = env->GetArrayLength(data);

    // Validate ELF magic
    if (dataLength < 20 || bytes[0] != 0x7F || bytes[1] != 'E' || 
        bytes[2] != 'L' || bytes[3] != 'F') {
        env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
        return nullptr;
    }

    // Extract ELF header fields
    jint result_data[] = {
        bytes[4],           // e_ident[EI_CLASS] - 1=32-bit, 2=64-bit
        bytes[5],           // e_ident[EI_DATA] - 1=little-endian, 2=big-endian
        bytes[6],           // e_ident[EI_VERSION]
        bytes[7],           // e_ident[EI_OSABI]
        bytes[8]            // e_ident[EI_ABIVERSION]
    };

    jintArray result = env->NewIntArray(5);
    env->SetIntArrayRegion(result, 0, 5, result_data);

    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    return result;
}

/**
 * JNI function to extract strings from binary
 * Finds null-terminated ASCII strings
 *
 * @param env JNI environment
 * @param clazz Class reference
 * @param data Byte array containing binary data
 * @param minLength Minimum string length to extract
 * @return Array of extracted strings
 */
JNIEXPORT jobjectArray JNICALL
Java_com_example_soanalyzer_utils_NativeDisassembler_extractStrings(
        JNIEnv *env,
        jclass clazz,
        jbyteArray data,
        jint minLength) {

    jbyte *bytes = env->GetByteArrayElements(data, nullptr);
    jsize dataLength = env->GetArrayLength(data);

    std::vector<std::string> strings;
    std::string current;

    for (jsize i = 0; i < dataLength; i++) {
        unsigned char c = (unsigned char)bytes[i];
        
        // ASCII printable characters
        if (c >= 32 && c <= 126) {
            current += (char)c;
        } else if (c == 0 && current.length() >= minLength) {
            // Null terminator - end of string
            strings.push_back(current);
            current.clear();
        } else {
            current.clear();
        }
    }

    // Create Java string array
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(strings.size(), stringClass, nullptr);

    for (size_t i = 0; i < strings.size(); i++) {
        jstring str = env->NewStringUTF(strings[i].c_str());
        env->SetObjectArrayElement(result, i, str);
        env->DeleteLocalRef(str);
    }

    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    return result;
}

/**
 * JNI function to calculate file hash (SHA-256)
 * For binary integrity verification
 *
 * @param env JNI environment
 * @param clazz Class reference
 * @param data Byte array containing file data
 * @return Hex string representation of SHA-256 hash
 */
JNIEXPORT jstring JNICALL
Java_com_example_soanalyzer_utils_NativeDisassembler_calculateHash(
        JNIEnv *env,
        jclass clazz,
        jbyteArray data) {

    // TODO: Implement SHA-256 hashing
    // For now, return placeholder
    return env->NewStringUTF("0000000000000000000000000000000000000000000000000000000000000000");
}

} // extern "C"
