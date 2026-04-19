/**
 * @file fastaudiocapture.cpp
 * @brief Native Windows audio capture using WASAPI
 * 
 * Stub implementation for v1.0.0
 * Full WASAPI loopback capture coming in v1.1
 */

#include <jni.h>
#include <windows.h>
#include <string>

// Helper: Convert UTF-8 to wide string
std::wstring UTF8ToWString(JNIEnv* env, jstring str) {
    if (!str) return L"";
    const char* chars = env->GetStringUTFChars(str, nullptr);
    if (!chars) return L"";
    int len = MultiByteToWideChar(CP_UTF8, 0, chars, -1, nullptr, 0);
    std::wstring result(len - 1, 0);
    MultiByteToWideChar(CP_UTF8, 0, chars, -1, &result[0], len);
    env->ReleaseStringUTFChars(str, chars);
    return result;
}

extern "C" {

JNIEXPORT jlong JNICALL Java_fastaudio_FastAudioCapture_createCapture(JNIEnv* env, jclass clazz) {
    // Stub: Return dummy handle
    return 1;
}

JNIEXPORT void JNICALL Java_fastaudio_FastAudioCapture_destroyCapture(JNIEnv* env, jclass clazz, jlong handle) {
    // Stub: Nothing to do
}

JNIEXPORT jboolean JNICALL Java_fastaudio_FastAudioCapture_startRecording(JNIEnv* env, jclass clazz, 
    jlong handle, jstring deviceId, jint sampleRate, jint channels, jint bitsPerSample) {
    // Stub: Always return true
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_fastaudio_FastAudioCapture_stopRecording(JNIEnv* env, jclass clazz, jlong handle) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_fastaudio_FastAudioCapture_isRecording(JNIEnv* env, jclass clazz, jlong handle) {
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fastaudio_FastAudioCapture_saveToFile(JNIEnv* env, jclass clazz, jlong handle, jstring filePath) {
    return JNI_TRUE;
}

JNIEXPORT jfloat JNICALL Java_fastaudio_FastAudioCapture_getLevel(JNIEnv* env, jclass clazz, jlong handle) {
    // Stub: Return silence
    return 0.0f;
}

JNIEXPORT jobjectArray JNICALL Java_fastaudio_FastAudioCapture_getCaptureDevices(JNIEnv* env, jclass clazz) {
    jobjectArray result = env->NewObjectArray(2, env->FindClass("java/lang/String"), nullptr);
    jstring micDevice = env->NewStringUTF("Microphone");
    jstring loopDevice = env->NewStringUTF("System Audio (Loopback)");
    env->SetObjectArrayElement(result, 0, micDevice);
    env->SetObjectArrayElement(result, 1, loopDevice);
    env->DeleteLocalRef(micDevice);
    env->DeleteLocalRef(loopDevice);
    return result;
}

JNIEXPORT jstring JNICALL Java_fastaudio_FastAudioCapture_getDefaultDevice(JNIEnv* env, jclass clazz) {
    return env->NewStringUTF("Microphone");
}

} // extern "C"
