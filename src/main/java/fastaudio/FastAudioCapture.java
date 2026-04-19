package fastaudio;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * FastAudioCapture - Native audio capture/recording for Java.
 * 
 * <p>Real-time audio capture using Windows WASAPI. Record from microphone
 * or system audio (loopback) with minimal latency.</p>
 * 
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Microphone recording</li>
 *   <li>System audio capture (loopback)</li>
 *   <li>Real-time audio callback</li>
 *   <li>Save to WAV file</li>
 *   <li>Level meter / VU meter</li>
 * </ul>
 * 
 * @author FastJava Team
 * @version 1.0.0
 */
public class FastAudioCapture {
    
    private static final String LIBRARY_NAME = "fastaudiocapture";
    private long nativeHandle;
    
    static {
        loadNativeLibrary();
    }
    
    private static void loadNativeLibrary() {
        try {
            System.loadLibrary(LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            try {
                String libResource = "/" + LIBRARY_NAME + ".dll";
                try (InputStream in = FastAudioCapture.class.getResourceAsStream(libResource)) {
                    if (in == null) {
                        throw new RuntimeException("Native library not found: " + libResource);
                    }
                    Path tempDir = Files.createTempDirectory("fastaudio");
                    Path tempLib = tempDir.resolve(LIBRARY_NAME + ".dll");
                    Files.copy(in, tempLib, StandardCopyOption.REPLACE_EXISTING);
                    tempLib.toFile().deleteOnExit();
                    tempDir.toFile().deleteOnExit();
                    System.load(tempLib.toString());
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load native library", ex);
            }
        }
    }
    
    // Native methods
    private static native long createCapture();
    private static native void destroyCapture(long handle);
    private static native boolean startRecording(long handle, String deviceId, int sampleRate, int channels, int bitsPerSample);
    private static native boolean stopRecording(long handle);
    private static native boolean isRecording(long handle);
    private static native boolean saveToFile(long handle, String filePath);
    private static native float getLevel(long handle);
    private static native String[] nativeGetCaptureDevices();
    private static native String nativeGetDefaultDevice();
    
    /**
     * Audio capture callback interface.
     */
    public interface AudioCallback {
        /**
         * Called when audio data is captured.
         * @param samples Audio samples (16-bit PCM, -32768 to 32767)
         * @param timestamp Capture timestamp in nanoseconds
         */
        void onAudioData(short[] samples, long timestamp);
    }
    
    /**
     * Create a new audio capture instance.
     */
    public FastAudioCapture() {
        this.nativeHandle = createCapture();
        if (this.nativeHandle == 0) {
            throw new RuntimeException("Failed to create audio capture");
        }
    }
    
    /**
     * Start recording from default microphone.
     * @param sampleRate Sample rate (e.g., 44100, 48000)
     * @param channels Number of channels (1=mono, 2=stereo)
     * @param bitsPerSample Bits per sample (16 or 24)
     * @return true if started successfully
     */
    public boolean startRecording(int sampleRate, int channels, int bitsPerSample) {
        checkHandle();
        return startRecording(nativeHandle, null, sampleRate, channels, bitsPerSample);
    }
    
    /**
     * Start recording from specific device.
     * @param deviceId Device ID from getCaptureDevices()
     * @param sampleRate Sample rate
     * @param channels Number of channels
     * @param bitsPerSample Bits per sample
     * @return true if started successfully
     */
    public boolean startRecording(String deviceId, int sampleRate, int channels, int bitsPerSample) {
        checkHandle();
        return startRecording(nativeHandle, deviceId, sampleRate, channels, bitsPerSample);
    }
    
    /**
     * Start recording system audio (loopback).
     * Records everything playing on the speakers.
     * @param sampleRate Sample rate
     * @param channels Number of channels
     * @param bitsPerSample Bits per sample
     * @return true if started successfully
     */
    public boolean startSystemRecording(int sampleRate, int channels, int bitsPerSample) {
        checkHandle();
        // Loopback device is typically identified by null or special ID
        return startRecording(nativeHandle, "loopback", sampleRate, channels, bitsPerSample);
    }
    
    /**
     * Stop recording.
     * @return true if stopped successfully
     */
    public boolean stopRecording() {
        checkHandle();
        return stopRecording(nativeHandle);
    }
    
    /**
     * Check if currently recording.
     * @return true if recording
     */
    public boolean isRecording() {
        checkHandle();
        return isRecording(nativeHandle);
    }
    
    /**
     * Save recorded audio to WAV file.
     * @param filePath Output file path
     * @return true if saved successfully
     */
    public boolean saveToFile(String filePath) {
        checkHandle();
        return saveToFile(nativeHandle, filePath);
    }
    
    /**
     * Save recorded audio to File.
     * @param file Output file
     * @return true if saved successfully
     */
    public boolean saveToFile(File file) {
        return saveToFile(file.getAbsolutePath());
    }
    
    /**
     * Get current audio level (VU meter).
     * @return Level 0.0-1.0 (peak amplitude)
     */
    public float getLevel() {
        checkHandle();
        return getLevel(nativeHandle);
    }
    
    /**
     * Get list of available capture devices.
     * @return Array of device IDs
     */
    public static String[] getCaptureDevices() {
        return nativeGetCaptureDevices();
    }
    
    /**
     * Get default capture device.
     * @return Default device ID
     */
    public static String getDefaultDevice() {
        return nativeGetDefaultDevice();
    }
    
    /**
     * Close capture and release resources.
     */
    public void close() {
        if (nativeHandle != 0) {
            destroyCapture(nativeHandle);
            nativeHandle = 0;
        }
    }
    
    private void checkHandle() {
        if (nativeHandle == 0) {
            throw new IllegalStateException("Capture is closed");
        }
    }
    
}
