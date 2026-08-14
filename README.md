# FastAudioCapture 0.1.1 [ALPHA-2026-05-17] — High-Performance Native Audio Capture for Java



[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastAudioCapture/releases/tag/0.1.1)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)

[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()

[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)



**🎙️ A low-latency native audio capture module for the FastJava ecosystem. High-fidelity input via WASAPI and DirectSound.**



**FastAudioCapture** provides real-time access to system and microphone audio with minimal latency. Built for

vision/audio synchronization and high-performance processing.



[![FastFileIndex Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)



---



## Quick Start



```bash

# Clone the repository

git clone https://github.com/andrestubbe/FastAudioCapture.git



# Build the project

cd FastAudioCapture

mvn clean install

```

---



---



## Table of Contents



- [Features](#features)

- [Quick Start](#quick-start)

- [Installation](#installation)

- [Build from Source](#build-from-source)

- [License](#license)



---



## Features



- **⏱️ Low Latency**: Native WASAPI integration for minimal delay.

- **🔊 High Fidelity**: Support for 44.1kHz, 48kHz, and high-bitrate capture.

- **📥 Zero-Copy**: Direct access to audio buffers via DirectByteBuffer.

- **🔗 Ecosystem Ready**: Designed for use with FastAudioPlayer and FastSTT.



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

---

## Documentation



* **[COMPILE.md](COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).

* **[REFERENCE.md](docs/REFERENCE.md)**: Full API descriptions, border configurations, and codepoint index.

* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The engineering rationale for zero-allocation performance.

* **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and planned features.



---



## Platform Support



| Platform      | Status            |

|---------------|-------------------|

| Windows 10/11 | ✅ Fully Supported |

| Linux         | 🔗 Planned        |

| macOS         | 🔗 Planned        |



---



## License



MIT License  See [LICENSE](LICENSE) file for details.



---



## Related Projects



- [FastCore](https://github.com/andrestubbe/FastCore)  Native Library Loader for Java

- [FastAudioPlayer](https://github.com/andrestubbe/FastAudioPlayer)  Native Windows WASAPI Audio Playback for Java

- [FastTTS](https://github.com/andrestubbe/FastTTS)  High-Performance Native Windows TTS API for Java

- [FastSTT](https://github.com/andrestubbe/FastSTT)  Ultra-Fast Native Speech-to-Text for Java

- [FastWakeWord](https://github.com/andrestubbe/FastWakeWord)



---



**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*

