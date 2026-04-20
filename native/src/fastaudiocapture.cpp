/**
 * @file fastaudiocapture.cpp
 * @brief Native Windows audio capture using WASAPI
 * 
 * Real WASAPI implementation for microphone and system audio capture.
 * Supports loopback recording (capture what you hear).
 */

#include <jni.h>
#include <windows.h>
#include <mmdeviceapi.h>
#include <audioclient.h>
#include <audiopolicy.h>
#include <functiondiscoverykeys_devpkey.h>
#include <string>
#include <vector>
#include <thread>
#include <atomic>
#include <cstring>
#include <chrono>

#pragma comment(lib, "ole32.lib")

// Audio Capture State
struct AudioCapture {
    IMMDeviceEnumerator* deviceEnumerator = nullptr;
    IMMDevice* captureDevice = nullptr;
    IAudioClient* audioClient = nullptr;
    IAudioCaptureClient* captureClient = nullptr;
    
    std::atomic<bool> isRecording{false};
    std::atomic<float> currentLevel{0.0f};
    
    WAVEFORMATEX waveFormat{};
    std::vector<BYTE> recordedData;
    
    std::thread captureThread;
    HANDLE stopEvent = nullptr;
    
    // Recording parameters
    UINT32 sampleRate = 44100;
    UINT16 channels = 2;
    UINT16 bitsPerSample = 16;
    bool isLoopback = false; // false = microphone, true = system audio
    
    ~AudioCapture() {
        stopRecording();
        releaseAll();
        if (stopEvent) {
            CloseHandle(stopEvent);
        }
    }
    
    void releaseResources() {
        // Note: deviceEnumerator is NOT released here - we keep it for reuse
        // It's only released in the destructor
        if (captureClient) {
            captureClient->Release();
            captureClient = nullptr;
        }
        if (audioClient) {
            audioClient->Release();
            audioClient = nullptr;
        }
        if (captureDevice) {
            captureDevice->Release();
            captureDevice = nullptr;
        }
    }
    
    void releaseAll() {
        releaseResources();
        if (deviceEnumerator) {
            deviceEnumerator->Release();
            deviceEnumerator = nullptr;
        }
    }
    
    void stopRecording() {
        isRecording = false;
        if (stopEvent) {
            SetEvent(stopEvent);
        }
        if (captureThread.joinable()) {
            captureThread.join();
        }
        if (audioClient) {
            audioClient->Stop();
        }
    }
    
    // Calculate RMS level from 32-bit float audio data (WASAPI format)
    void calculateLevel(const BYTE* data, UINT32 numFrames) {
        const float* samples = reinterpret_cast<const float*>(data);
        int totalSamples = numFrames * channels;
        
        float sum = 0.0f;
        for (int i = 0; i < totalSamples; i++) {
            float sample = samples[i];
            sum += sample * sample;
        }
        
        float rms = sqrt(sum / totalSamples);
        currentLevel = rms;
    }
};

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

// Helper: Convert wide string to jstring
jstring WStringToJString(JNIEnv* env, const std::wstring& wstr) {
    int len = WideCharToMultiByte(CP_UTF8, 0, wstr.c_str(), -1, nullptr, 0, nullptr, nullptr);
    std::string result(len - 1, 0);
    WideCharToMultiByte(CP_UTF8, 0, wstr.c_str(), -1, &result[0], len, nullptr, nullptr);
    return env->NewStringUTF(result.c_str());
}

// Capture thread function
void CaptureThread(AudioCapture* capture) {
    HRESULT hr;
    UINT32 packetLength = 0;
    BYTE* pData = nullptr;
    DWORD flags = 0;
    UINT64 devicePosition;
    UINT64 qpcPosition;
    
    // Event-driven capture loop
    HANDLE waitArray[1] = { capture->stopEvent };
    
    while (capture->isRecording) {
        // Safety check
        if (!capture->captureClient) break;
        
        // Wait with timeout (100ms)
        DWORD waitResult = WaitForMultipleObjects(1, waitArray, FALSE, 100);
        
        if (waitResult == WAIT_OBJECT_0) {
            // Stop event signaled
            break;
        }
        
        // Get next packet size
        hr = capture->captureClient->GetNextPacketSize(&packetLength);
        if (FAILED(hr)) continue;
        
        // Process all available packets
        while (packetLength != 0 && capture->isRecording) {
            // Get buffer
            hr = capture->captureClient->GetBuffer(
                &pData,
                &packetLength,
                &flags,
                &devicePosition,
                &qpcPosition
            );
            if (FAILED(hr) || !pData) break;
            
            // Calculate size in bytes (safety check)
            UINT32 blockAlign = capture->waveFormat.nBlockAlign;
            if (blockAlign == 0) blockAlign = 4; // default to 16-bit stereo
            UINT32 bytesToCopy = packetLength * blockAlign;
            
            // Calculate level and copy/convert data
            if (!(flags & AUDCLNT_BUFFERFLAGS_SILENT) && bytesToCopy > 0) {
                capture->calculateLevel(pData, packetLength);
                
                // Check if we need to convert 32-bit float to 16-bit PCM
                if (capture->waveFormat.wBitsPerSample == 32) {
                    // Convert float to 16-bit PCM
                    const float* floatData = reinterpret_cast<const float*>(pData);
                    UINT32 numSamples = packetLength * capture->waveFormat.nChannels;
                    size_t currentSize = capture->recordedData.size();
                    capture->recordedData.resize(currentSize + (numSamples * 2));
                    short* pcmData = reinterpret_cast<short*>(capture->recordedData.data() + currentSize);
                    
                    for (UINT32 i = 0; i < numSamples; i++) {
                        float sample = floatData[i];
                        if (sample > 1.0f) sample = 1.0f;
                        if (sample < -1.0f) sample = -1.0f;
                        pcmData[i] = static_cast<short>(sample * 32767.0f);
                    }
                } else {
                    // Copy as-is (already 16-bit PCM)
                    size_t currentSize = capture->recordedData.size();
                    capture->recordedData.resize(currentSize + bytesToCopy);
                    memcpy(capture->recordedData.data() + currentSize, pData, bytesToCopy);
                }
            } else if (bytesToCopy > 0) {
                // Silent frame - add silence to buffer (16-bit PCM size)
                UINT32 numSamples = packetLength * capture->waveFormat.nChannels;
                size_t currentSize = capture->recordedData.size();
                capture->recordedData.resize(currentSize + (numSamples * 2));
                memset(capture->recordedData.data() + currentSize, 0, numSamples * 2);
                capture->currentLevel = 0.0f;
            }
            
            // Release buffer
            hr = capture->captureClient->ReleaseBuffer(packetLength);
            if (FAILED(hr)) break;
            
            // Get next packet
            hr = capture->captureClient->GetNextPacketSize(&packetLength);
            if (FAILED(hr)) break;
        }
    }
}

// Write WAV file - converts 32-bit float to 16-bit PCM for compatibility
bool WriteWavFile(const wchar_t* filePath, const std::vector<BYTE>& audioData, const WAVEFORMATEX& format) {
    HANDLE hFile = CreateFileW(filePath, GENERIC_WRITE, 0, nullptr, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, nullptr);
    if (hFile == INVALID_HANDLE_VALUE) return false;
    
    // Prepare 16-bit PCM format
    WAVEFORMATEX outFormat;
    outFormat.wFormatTag = WAVE_FORMAT_PCM;
    outFormat.nChannels = format.nChannels;
    outFormat.nSamplesPerSec = format.nSamplesPerSec;
    outFormat.wBitsPerSample = 16;
    outFormat.nBlockAlign = outFormat.nChannels * 2;
    outFormat.nAvgBytesPerSec = outFormat.nSamplesPerSec * outFormat.nBlockAlign;
    outFormat.cbSize = 0;
    
    // Data is already converted to 16-bit PCM in capture thread
    // Just use it directly
    const std::vector<BYTE>& outData = audioData;
    
    DWORD bytesWritten;
    DWORD dataSize = static_cast<DWORD>(outData.size());
    DWORD riffSize = 36 + dataSize;
    
    // Write RIFF header
    WriteFile(hFile, "RIFF", 4, &bytesWritten, nullptr);
    WriteFile(hFile, &riffSize, 4, &bytesWritten, nullptr);
    WriteFile(hFile, "WAVE", 4, &bytesWritten, nullptr);
    
    // Write fmt chunk
    WriteFile(hFile, "fmt ", 4, &bytesWritten, nullptr);
    DWORD fmtSize = 16;
    WriteFile(hFile, &fmtSize, 4, &bytesWritten, nullptr);
    WriteFile(hFile, &outFormat.wFormatTag, 2, &bytesWritten, nullptr);
    WriteFile(hFile, &outFormat.nChannels, 2, &bytesWritten, nullptr);
    WriteFile(hFile, &outFormat.nSamplesPerSec, 4, &bytesWritten, nullptr);
    WriteFile(hFile, &outFormat.nAvgBytesPerSec, 4, &bytesWritten, nullptr);
    WriteFile(hFile, &outFormat.nBlockAlign, 2, &bytesWritten, nullptr);
    WriteFile(hFile, &outFormat.wBitsPerSample, 2, &bytesWritten, nullptr);
    
    // Write data chunk
    WriteFile(hFile, "data", 4, &bytesWritten, nullptr);
    WriteFile(hFile, &dataSize, 4, &bytesWritten, nullptr);
    WriteFile(hFile, outData.data(), dataSize, &bytesWritten, nullptr);
    
    CloseHandle(hFile);
    return bytesWritten == dataSize;
}

extern "C" {

JNIEXPORT jlong JNICALL Java_fastaudio_FastAudioCapture_createCapture(JNIEnv* env, jclass clazz) {
    AudioCapture* capture = new AudioCapture();
    
    // Initialize COM - S_FALSE is also OK (already initialized)
    HRESULT hr = CoInitializeEx(nullptr, COINIT_MULTITHREADED);
    if (FAILED(hr) && hr != RPC_E_CHANGED_MODE) {
        delete capture;
        return 0;
    }
    
    // Create stop event
    capture->stopEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
    if (!capture->stopEvent) {
        CoUninitialize();
        delete capture;
        return 0;
    }
    
    // Create device enumerator
    hr = CoCreateInstance(
        __uuidof(MMDeviceEnumerator),
        nullptr,
        CLSCTX_ALL,
        __uuidof(IMMDeviceEnumerator),
        (void**)&capture->deviceEnumerator
    );
    
    if (FAILED(hr)) {
        CloseHandle(capture->stopEvent);
        CoUninitialize();
        delete capture;
        return 0;
    }
    
    return reinterpret_cast<jlong>(capture);
}

JNIEXPORT void JNICALL Java_fastaudio_FastAudioCapture_destroyCapture(JNIEnv* env, jclass clazz, jlong handle) {
    if (handle) {
        AudioCapture* capture = reinterpret_cast<AudioCapture*>(handle);
        delete capture;
        CoUninitialize();
    }
}

JNIEXPORT jboolean JNICALL Java_fastaudio_FastAudioCapture_startRecording(
    JNIEnv* env, 
    jclass clazz, 
    jlong handle, 
    jstring deviceId, 
    jint sampleRate, 
    jint channels, 
    jint bitsPerSample
) {
    
    if (!handle) {
        return JNI_FALSE;
    }
    
    AudioCapture* capture = reinterpret_cast<AudioCapture*>(handle);
    
    // Stop existing recording
    capture->stopRecording();
    capture->releaseResources();
    capture->recordedData.clear();
    capture->currentLevel = 0.0f;
    
    // Store parameters
    capture->sampleRate = sampleRate;
    capture->channels = static_cast<UINT16>(channels);
    capture->bitsPerSample = static_cast<UINT16>(bitsPerSample);
    
    // Determine if this is loopback (system audio) capture
    std::wstring deviceParam = UTF8ToWString(env, deviceId);
    capture->isLoopback = (deviceParam.find(L"loopback") != std::wstring::npos || 
                           deviceParam.find(L"Loopback") != std::wstring::npos || 
                           deviceParam.find(L"System") != std::wstring::npos);
    
    // Get device - for loopback we need the render endpoint
    EDataFlow dataFlow = capture->isLoopback ? eRender : eCapture;
    DWORD stateMask = DEVICE_STATE_ACTIVE;
    
    HRESULT hr;
    bool useDefault = deviceParam.empty() || (deviceParam == L"Default") || (deviceParam == L"Microphone");
    if (useDefault) {
        // Use default device
        hr = capture->deviceEnumerator->GetDefaultAudioEndpoint(
            dataFlow, 
            eConsole, 
            &capture->captureDevice
        );
    } else {
        // Try to find device by name
        IMMDeviceCollection* devices = nullptr;
        if (!capture->deviceEnumerator) {
            return JNI_FALSE;
        }
        hr = capture->deviceEnumerator->EnumAudioEndpoints(dataFlow, stateMask, &devices);
        if (SUCCEEDED(hr)) {
            UINT count;
            devices->GetCount(&count);
            
            for (UINT i = 0; i < count; i++) {
                IMMDevice* pDevice = nullptr;
                if (SUCCEEDED(devices->Item(i, &pDevice))) {
                    IPropertyStore* props = nullptr;
                    if (SUCCEEDED(pDevice->OpenPropertyStore(STGM_READ, &props))) {
                        PROPVARIANT varName;
                        PropVariantInit(&varName);
                        if (SUCCEEDED(props->GetValue(PKEY_Device_FriendlyName, &varName))) {
                            // Check if it's a string type
                            if (varName.vt == VT_LPWSTR && varName.pwszVal) {
                                if (wcscmp(deviceParam.c_str(), varName.pwszVal) == 0) {
                                    capture->captureDevice = pDevice;
                                    PropVariantClear(&varName);
                                    props->Release();
                                    break;
                                }
                            }
                            PropVariantClear(&varName);
                        }
                        props->Release();
                    }
                    if (capture->captureDevice != pDevice) {
                        pDevice->Release();
                    }
                }
            }
            devices->Release();
        }
        
        // If not found, use default
        if (!capture->captureDevice) {
            hr = capture->deviceEnumerator->GetDefaultAudioEndpoint(
                dataFlow, eConsole, &capture->captureDevice
            );
        }
    }
    
    if (FAILED(hr) || !capture->captureDevice) {
        return JNI_FALSE;
    }
    
    // Activate audio client
    hr = capture->captureDevice->Activate(
        __uuidof(IAudioClient), CLSCTX_ALL, nullptr, (void**)&capture->audioClient
    );
    if (FAILED(hr)) {
        return JNI_FALSE;
    }
    
    // Get mix format - this is what the device supports
    WAVEFORMATEX* mixFormat = nullptr;
    hr = capture->audioClient->GetMixFormat(&mixFormat);
    if (FAILED(hr)) return JNI_FALSE;
    
    // Store the actual format we're using
    capture->waveFormat = *mixFormat;
    capture->sampleRate = mixFormat->nSamplesPerSec;
    capture->channels = mixFormat->nChannels;
    capture->bitsPerSample = mixFormat->wBitsPerSample;
    
    // Initialize audio client with mix format (guaranteed to work)
    REFERENCE_TIME bufferDuration = 10000000; // 1 second
    DWORD streamFlags = capture->isLoopback ? AUDCLNT_STREAMFLAGS_LOOPBACK : 0;
    
    fprintf(stderr, "[Capture] Format: Tag=%d, Bits=%d, Align=%d\n",
            capture->waveFormat.wFormatTag, capture->waveFormat.wBitsPerSample, capture->waveFormat.nBlockAlign);
    fprintf(stderr, "[Capture] Initializing audio client: %dHz, %dch, %dbits (mix format), loopback=%d\n",
            capture->sampleRate, capture->channels, capture->bitsPerSample, capture->isLoopback);
    
    hr = capture->audioClient->Initialize(
        AUDCLNT_SHAREMODE_SHARED,
        streamFlags,
        bufferDuration,
        0,
        mixFormat,  // Use the mix format directly!
        nullptr
    );
    
    CoTaskMemFree(mixFormat);
    if (FAILED(hr)) {
        return JNI_FALSE;
    }
    
    
    // Get capture client
    hr = capture->audioClient->GetService(
        __uuidof(IAudioCaptureClient), (void**)&capture->captureClient
    );
    if (FAILED(hr)) {
        return JNI_FALSE;
    }
    
    
    // Start recording
    hr = capture->audioClient->Start();
    if (FAILED(hr)) {
        return JNI_FALSE;
    }
    
    
    capture->isRecording = true;
    ResetEvent(capture->stopEvent);
    
    // Start capture thread
    capture->captureThread = std::thread(CaptureThread, capture);
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_fastaudio_FastAudioCapture_stopRecording(JNIEnv* env, jclass clazz, jlong handle) {
    if (!handle) return JNI_FALSE;
    
    AudioCapture* capture = reinterpret_cast<AudioCapture*>(handle);
    
    if (!capture->isRecording) return JNI_FALSE;
    
    capture->stopRecording();
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_fastaudio_FastAudioCapture_isRecording(JNIEnv* env, jclass clazz, jlong handle) {
    if (!handle) return JNI_FALSE;
    
    AudioCapture* capture = reinterpret_cast<AudioCapture*>(handle);
    return capture->isRecording ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_fastaudio_FastAudioCapture_saveToFile(JNIEnv* env, jclass clazz, jlong handle, jstring filePath) {
    if (!handle) return JNI_FALSE;
    
    AudioCapture* capture = reinterpret_cast<AudioCapture*>(handle);
    
    // Stop recording first
    if (capture->isRecording) {
        capture->stopRecording();
    }
    
    // Check if we have data
    if (capture->recordedData.empty()) {
        return JNI_FALSE;
    }
    
    // Get file path
    std::wstring path = UTF8ToWString(env, filePath);
    if (path.empty()) return JNI_FALSE;
    
    // Write WAV file
    if (WriteWavFile(path.c_str(), capture->recordedData, capture->waveFormat)) {
        return JNI_TRUE;
    }
    
    return JNI_FALSE;
}

JNIEXPORT jfloat JNICALL Java_fastaudio_FastAudioCapture_getLevel(JNIEnv* env, jclass clazz, jlong handle) {
    if (!handle) return 0.0f;
    
    AudioCapture* capture = reinterpret_cast<AudioCapture*>(handle);
    return capture->currentLevel.load();
}

JNIEXPORT jobjectArray JNICALL Java_fastaudio_FastAudioCapture_nativeGetCaptureDevices(JNIEnv* env, jclass clazz) {
    CoInitializeEx(nullptr, COINIT_APARTMENTTHREADED);
    
    IMMDeviceEnumerator* enumerator = nullptr;
    
    HRESULT hr = CoCreateInstance(
        __uuidof(MMDeviceEnumerator),
        nullptr,
        CLSCTX_ALL,
        __uuidof(IMMDeviceEnumerator),
        (void**)&enumerator
    );
    
    if (FAILED(hr)) {
        CoUninitialize();
        jobjectArray result = env->NewObjectArray(2, env->FindClass("java/lang/String"), nullptr);
        jstring mic = env->NewStringUTF("Microphone");
        jstring loop = env->NewStringUTF("System Audio (Loopback)");
        env->SetObjectArrayElement(result, 0, mic);
        env->SetObjectArrayElement(result, 1, loop);
        return result;
    }
    
    std::vector<std::wstring> deviceNames;
    
    // Get capture devices (microphones)
    IMMDeviceCollection* devices = nullptr;
    hr = enumerator->EnumAudioEndpoints(eCapture, DEVICE_STATE_ACTIVE, &devices);
    if (SUCCEEDED(hr)) {
        UINT count;
        devices->GetCount(&count);
        
        for (UINT i = 0; i < count; i++) {
            IMMDevice* device = nullptr;
            if (SUCCEEDED(devices->Item(i, &device))) {
                IPropertyStore* props = nullptr;
                if (SUCCEEDED(device->OpenPropertyStore(STGM_READ, &props))) {
                    PROPVARIANT varName;
                    PropVariantInit(&varName);
                    if (SUCCEEDED(props->GetValue(PKEY_Device_FriendlyName, &varName))) {
                        deviceNames.push_back(varName.pwszVal);
                        PropVariantClear(&varName);
                    }
                    props->Release();
                }
                device->Release();
            }
        }
        devices->Release();
    }
    
    // Add loopback option if we have render devices
    IMMDeviceCollection* renderDevices = nullptr;
    hr = enumerator->EnumAudioEndpoints(eRender, DEVICE_STATE_ACTIVE, &renderDevices);
    if (SUCCEEDED(hr)) {
        UINT count;
        renderDevices->GetCount(&count);
        if (count > 0) {
            deviceNames.push_back(L"System Audio (Loopback)");
        }
        renderDevices->Release();
    }
    
    enumerator->Release();
    CoUninitialize();
    
    // Create result array
    jint size = deviceNames.empty() ? 1 : static_cast<jint>(deviceNames.size());
    jobjectArray result = env->NewObjectArray(size, env->FindClass("java/lang/String"), nullptr);
    
    if (deviceNames.empty()) {
        jstring def = env->NewStringUTF("Microphone");
        env->SetObjectArrayElement(result, 0, def);
    } else {
        for (jint i = 0; i < size; i++) {
            jstring name = WStringToJString(env, deviceNames[i]);
            env->SetObjectArrayElement(result, i, name);
        }
    }
    
    return result;
}

JNIEXPORT jstring JNICALL Java_fastaudio_FastAudioCapture_nativeGetDefaultDevice(JNIEnv* env, jclass clazz) {
    CoInitializeEx(nullptr, COINIT_APARTMENTTHREADED);
    
    IMMDeviceEnumerator* enumerator = nullptr;
    IMMDevice* device = nullptr;
    std::wstring defaultName = L"Microphone";
    
    HRESULT hr = CoCreateInstance(
        __uuidof(MMDeviceEnumerator),
        nullptr,
        CLSCTX_ALL,
        __uuidof(IMMDeviceEnumerator),
        (void**)&enumerator
    );
    
    if (SUCCEEDED(hr)) {
        hr = enumerator->GetDefaultAudioEndpoint(eCapture, eConsole, &device);
        if (SUCCEEDED(hr)) {
            IPropertyStore* props = nullptr;
            if (SUCCEEDED(device->OpenPropertyStore(STGM_READ, &props))) {
                PROPVARIANT varName;
                PropVariantInit(&varName);
                if (SUCCEEDED(props->GetValue(PKEY_Device_FriendlyName, &varName))) {
                    defaultName = varName.pwszVal;
                    PropVariantClear(&varName);
                }
                props->Release();
            }
            device->Release();
        }
        enumerator->Release();
    }
    
    CoUninitialize();
    return WStringToJString(env, defaultName);
}

} // extern "C"
