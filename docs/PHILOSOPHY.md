# FastAudioCapture Design Philosophy

`FastAudioCapture` communicates directly with Windows Audio Session API (WASAPI) and DirectSound input endpoints, bypassing JavaSound's high-overhead input layer to achieve sub-5ms capture latency with zero Garbage Collection pressure.

---

## Core Engineering Principles

1. **Zero-Copy DirectByteBuffer Streaming**  
   Input audio frames from hardware drivers are written directly into off-heap native memory buffers and passed to Java via `DirectByteBuffer`, avoiding JVM heap array copies.

2. **Sub-5ms Input Latency**  
   Low-overhead COM input thread dispatching enables sub-5ms latency for real-time speech recognition (**FastSTT**) and streaming pipelines.

3. **FastJava Interoperability**  
   Captured PCM buffers seamlessly feed into **FastSIMD** vector normalization engines and **FastAudioProcess** frame chunkers.
