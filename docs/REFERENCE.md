# FastAudioCapture API Reference Manual

`FastAudioCapture` provides native low-latency WASAPI audio capture for Java applications with zero GC pressure.

---

## 1. Core API

### `startCapture` / `stopCapture`
```java
public void startCapture(AudioCaptureListener listener)
```
Captures 16kHz / 48kHz microphone audio streams in real-time using off-heap DirectByteBuffers.
