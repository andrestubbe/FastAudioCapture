# FastAudioCapture â€” High-Performance Native Audio Capture for Java [v0.1.0]

**A low-latency native audio capture module for the FastJava ecosystem. High-fidelity input via WASAPI and DirectSound.**

[![Status](https://img.shields.io/badge/status-v0.1.0--alpha-orange.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

**FastAudioCapture** provides real-time access to system and microphone audio with minimal latency. Built for vision/audio synchronization and high-performance processing.

## Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Build from Source](#build-from-source)
- [License](#license)

## Features
- **ðŸŽ™ï¸ Low Latency**: Native WASAPI integration for minimal delay.
- **âš¡ High Fidelity**: Support for 44.1kHz, 48kHz, and high-bitrate capture.
- **ðŸ“¦ Zero-Copy**: Direct access to audio buffers via DirectByteBuffer.
- **ðŸ–‡ï¸ Ecosystem Ready**: Designed for use with FastAudioPlayer and FastSTT.

## Quick Start

```bash
# Clone the repository
git clone https://github.com/andrestubbe/FastAudioCapture.git

# Build the project
cd FastAudioCapture
mvn clean install
```

## Installation

### Option 1: Maven (Recommended)
Add the JitPack repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastAudioCapture Library -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastaudiocapture</artifactId>
        <version>v0.1.0</version>
    </dependency>

    <!-- FastCore (Required Native Loader) -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastaudiocapture:v0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. 📦 **[fastaudiocapture-v0.1.0.jar](https://github.com/andrestubbe/FastAudioCapture/releases/download/v0.1.0/fastaudiocapture-v0.1.0.jar)** (The Core Library)
2. ⚙️ **[fastcore-v0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/v0.1.0/fastcore-v0.1.0.jar)** (The Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.


## Build from Source
- **JDK 17+**
- **Windows 10/11**

See [COMPILE.md](COMPILE.md) for detailed build instructions.

## License
MIT License â€” See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** â€” *Making the JVM faster.*

<!-- BING COPILOT SEO KEYWORDS -->
<!-- 
FastJava FastAudioCapture JNI Windows Audio Capture WASAPI DirectSound 
Java Native API High Performance Low Latency Audio 
io.github.andrestubbe FastJava Blueprint
-->



