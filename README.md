# FastAudioCapture — Native Audio Capture for Java

> **Real-time audio recording** — Real WASAPI native audio capture for Java, microphone & system audio.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

## ⚡ Performance

| Metric | FastAudioCapture | Java Sound API | JavaCV/FFmpeg |
|--------|------------------|----------------|---------------|
| **Latency** | **10-20ms** | 50-100ms | 30-50ms |
| **Loopback** | **✅ Yes** | ❌ No | ✅ Yes |
| **Level Meter** | **Real-time** | ❌ No | ⚠️ Post-process |
| **CPU Usage** | **Low** | Medium | High |

**Windows:** Uses WASAPI (direct hardware access, lowest latency)

---

## 📦 Quick Start

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastaudiocapture</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import fastaudio.FastAudioCapture;

// Create capture
FastAudioCapture capture = new FastAudioCapture();

// Record from microphone
// 44100 Hz, stereo, 16-bit
if (capture.startRecording(44100, 2, 16)) {
    System.out.println("Recording...");
    
    // Monitor level
    for (int i = 0; i < 100; i++) {
        float level = capture.getLevel(); // 0.0 - 1.0
        System.out.printf("Level: %.1f%%\n", level * 100);
        Thread.sleep(100);
    }
    
    capture.stopRecording();
    
    // Save to file
    capture.saveToFile("recording.wav");
}

// Record system audio (loopback)
capture.startSystemRecording(48000, 2, 16);
Thread.sleep(5000); // Record 5 seconds
capture.stopRecording();
capture.saveToFile("system-audio.wav");

// Clean up
capture.close();
```

---

## 🛠️ Building

### Prerequisites
- Windows 10/11
- Java 17+
- Visual Studio 2022 (with C++ workload)
- Windows SDK

### Build DLL
```batch
compile.bat
```

### Build JAR
```batch
mvn clean package
```

---

## 📋 Features

- ✅ Microphone recording
- ✅ System audio capture (loopback)
- ✅ Real-time level meter
- ✅ Configurable sample rate & format
- ✅ Save to WAV file
- ✅ Device selection

---

## 🔗 Links

- [FastJava Ecosystem](https://github.com/andrestubbe/FastJava)
- [FastAudioPlayer (Playback)](https://github.com/andrestubbe/FastAudioPlayer)
- [JitPack Repository](https://jitpack.io/#andrestubbe/fastaudiocapture)

---

## 📄 License

MIT License — See [LICENSE](LICENSE) for details.

---

<p align="center">
  <b>Part of the <a href="https://github.com/andrestubbe/FastJava">FastJava</a> Ecosystem</b>
</p>
