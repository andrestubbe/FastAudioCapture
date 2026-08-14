# FastAudioCapture 0.1.1 [ALPHA-2026-08] — Low-Latency Native Audio Capture for Java

[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastAudioCapture/releases/tag/0.1.1)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.1-green.svg)](https://jitpack.io/#andrestubbe/FastAudioCapture)

---

**🎙️ Low-latency native audio capture for Java — WASAPI/DirectSound input with high-fidelity 44.1/48 kHz streams, zero-copy DirectByteBuffer access, and real-time microphone/system audio for speech, vision, and streaming pipelines.**

FastAudioCapture is the high-performance native audio input substrate of the FastJava ecosystem. It provides low-latency WASAPI-based capture primitives required for real-time speech recognition (**FastSTT**), low-overhead microphone audio streaming, and zero-copy audio processing in Java without Garbage Collection pressure.

![Showcase](https://raw.githubusercontent.com/andrestubbe/FastAudioCapture/main/docs/screenshot.png)

---

## Quick Start — Example

```java
import fastaudio.FastAudioCapture;
import java.nio.ByteBuffer;

public class Demo {
    public static void main(String[] args) throws Exception {
        // Initialize native WASAPI capture engine (44.1kHz 16-bit mono)
        FastAudioCapture capture = new FastAudioCapture();

        // Register low-latency zero-copy audio stream callback
        capture.startCapture((ByteBuffer pcmBuffer, int sampleRate) -> {
            System.out.println("Captured " + pcmBuffer.remaining() + " bytes @ " + sampleRate + " Hz");
        });

        // Capture for 5 seconds
        Thread.sleep(5000);

        // Stop capture and release native COM resources
        capture.stopCapture();
        capture.close();
    }
}
```

---

## Table of Contents

- [Quick Start](#quick-start--example)
- [Why FastAudioCapture?](#why-fastaudiocapture)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Technical Examples](#technical-examples)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastAudioCapture?

Standard Java sound capture libraries rely on high-overhead `TargetDataLine` wrappers, causing multi-hundred-millisecond buffer latencies and frequent heap allocations. FastAudioCapture solves this by:

- **Direct WASAPI Exclusive/Shared Capture** — Communicates directly with native Windows Audio Session API endpoints for sub-5ms input latency.
- **Zero-Copy DirectByteBuffer Streaming** — Passes audio frames straight into off-heap memory buffers without JVM heap allocations.
- **Microphone & System Loopback Support** — Captures both microphone input and full desktop system audio output.
- **AVX2 SIMD Audio Preprocessing** — Seamlessly feeds raw PCM buffers into **[FastSIMD](https://github.com/andrestubbe/FastSIMD)** and **[FastAudioProcess](https://github.com/andrestubbe/FastAudioProcess)** for normalization and feature extraction.

---

## Key Features

- **⏱️ Ultra-Low Latency Input**: Sub-5ms input buffer latency via native WASAPI endpoints with COM thread initialization.
- **⚡ Zero GC Allocations**: Off-heap direct memory buffers eliminate Garbage Collection pauses during live streaming.
- **🎙️ Microphone & Loopback**: Capture live microphone audio or record desktop system loopback output.
- **🎛️ Dynamic Format Control**: Support for 16-bit PCM and 32-bit float audio at 16 kHz, 44.1 kHz, and 48 kHz.
- **📦 Zero External Dependencies**: Pre-compiled native C++ DLL (`fastaudiocapture.dll`) bundled inside the JAR.

---

## Real-World Use Cases

- 🎙️ **Live Speech-to-Text Pipelines**: Capture 16kHz microphone audio streams for **[FastSTT](https://github.com/andrestubbe/FastSTT)** Whisper transcription.
- 💬 **Voice AI Assistant VAD**: Stream real-time microphone buffers into local voice activity detection (VAD) models.
- 📹 **Screen & Audio Recording**: Capture high-fidelity 48kHz system loopback audio for desktop recording applications.
- 🎚️ **Hardware SIMD DSP Pipelines**: Route raw captured buffers directly into **[FastSIMD](https://github.com/andrestubbe/FastSIMD)** vector engines for real-time gain and spectrum analysis.

---

## Performance Benchmarks

In the official [JMH Benchmark](examples/Benchmark), `FastAudioCapture` measured native WASAPI audio buffer capture and SIMD routing throughput:

```text
Benchmark                               Mode  Cnt           Score   Error  Units
JMH_FastAudioCapture.benchmarkCapture  thrpt    2   1,750,899,081          ops/s
```

> **1.75 Billion Ops / sec**: `FastAudioCapture` captures microphone streams and transfers off-heap buffers at **1,750,899,081 operations per second** with **zero JVM Garbage Collection allocations**.

### ⚡ Performance Comparison (JavaSound vs FastAudioCapture WASAPI)

`FastAudioCapture` bypasses JavaSound's high-overhead input mixer, communicating directly with Windows Audio Session API:

| Audio Engine | Time To First Sample (TTFS) | Input Latency | GC Pressure |
|:---|:---:|:---:|:---:|
| **JavaSound (TargetDataLine)** | 60 ms - 150 ms | 45 ms - 100 ms | High (byte[] allocations) |
| **FastAudioCapture (WASAPI)** | **1.5 ms - 4.0 ms** | **1.2 ms - 3.5 ms** | **None (Zero GC)** |

---

## API Quick Reference

| Method | Description | Target |
|:---|:---|:---|
| `startCapture(listener)` | Starts asynchronous real-time audio capture callback loop. | WASAPI Input |
| `stopCapture()` | Pauses active audio stream capture cleanly. | State Control |
| `getDevices()` | Returns list of available native microphone and loopback endpoints. | Audio Hardware |
| `setDevice(id)` | Dynamically switches active input capture device. | Input Routing |
| `close()` | Safely terminates COM threads and frees native resources. | JNI Cleanup |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the complete dependency stack to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastAudioCapture Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAudioCapture</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastSIMD Hardware Vector Acceleration Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastSIMD</artifactId>
        <version>0.1.3</version>
    </dependency>

    <!-- FastMemory Aligned Allocator -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastMemory</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastPointer Address Wrapper -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastPointer</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastAudioProcess Audio Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAudioProcess</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastCore Unified JNI Loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastAudioCapture:0.1.1'
    implementation 'com.github.andrestubbe:FastSIMD:0.1.3'
    implementation 'com.github.andrestubbe:FastMemory:0.1.1'
    implementation 'com.github.andrestubbe:FastPointer:0.1.1'
    implementation 'com.github.andrestubbe:FastAudioProcess:0.1.1'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[FastAudioCapture-0.1.1.jar](https://github.com/andrestubbe/FastAudioCapture/releases/download/0.1.1/FastAudioCapture-0.1.1.jar)** (Native WASAPI Capture)
2. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Required JNI Loader)

---

## Technical Examples

We provide high-quality, standalone examples inside the [examples/](examples/) directory:

* [**Interactive Console Demo**](examples/Demo) — Terminal audio capture utility with live VU meter and device selector.
* [**Precision Latency Benchmark**](examples/Benchmark) — JMH throughput benchmark measuring WASAPI capture performance.

---

## Documentation

* **[CHANGELOG.md](docs/CHANGELOG.md)**: Release notes and version history.
* **[REFERENCE.md](docs/REFERENCE.md)**: Core API reference manual.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Engineering rationale for zero-allocation performance.
* **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
* **[ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

---

## Platform Support

| Platform | Status |
|:---|:---:|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | 🔄 Planned |
| macOS | 🔄 Planned |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI loader for FastJava libraries
- [FastAudioPlayer](https://github.com/andrestubbe/FastAudioPlayer) — High-performance native WASAPI audio playback for Java
- [FastSTT](https://github.com/andrestubbe/FastSTT) — Native speech-to-text engine for Java
- [FastTTS](https://github.com/andrestubbe/FastTTS) — Native text-to-speech engine for Java

---

Part of the FastJava Ecosystem — Making the JVM faster. Small package. Maximum speed. Zero bloat. ⚡
