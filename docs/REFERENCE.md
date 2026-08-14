# FastAudioCapture API Reference Manual

`FastAudioCapture` provides low-latency native Windows WASAPI audio capture for Java applications with sub-5ms input latency and zero JVM Garbage Collection pressure.

---

## 1. Class Construction & Resource Management

### `FastAudioCapture()`
```java
public FastAudioCapture()
```
Constructs a new native WASAPI capture instance and initializes background COM input threads.

---

### `close()`
```java
public void close()
```
Safely terminates capture threads, releases native COM buffers, and frees off-heap memory.

---

## 2. Real-Time Audio Capture API

### `startCapture`
```java
public void startCapture(AudioCaptureListener listener) throws Exception
public void startCapture(AudioCaptureConfig config, AudioCaptureListener listener) throws Exception
```
Starts real-time asynchronous audio capture loop. Output frames are passed as zero-copy off-heap `ByteBuffer` instances to the provided callback listener.

---

### `stopCapture`
```java
public void stopCapture()
```
Stops active audio capture loop cleanly without dropping trailing buffer frames.

---

## 3. Device Query & Selection API

### `getDevices`
```java
public List<AudioDevice> getDevices()
```
Returns a list of all active native WASAPI input endpoint devices (microphones and system loopback drivers) available on the system.

---

### `setDevice`
```java
public void setDevice(String deviceId)
```
Dynamically switches the active microphone or loopback input device.

---

## 4. Audio Configuration & Formats

```java
public class AudioCaptureConfig {
    public int sampleRate; // 16000, 44100, 48000 Hz
    public int channels;   // 1 (Mono), 2 (Stereo)
    public int bitsPerSample; // 16-bit PCM, 32-bit Float
}
```
