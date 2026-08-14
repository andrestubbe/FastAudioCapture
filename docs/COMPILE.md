# FastAudioCapture Compilation Guide

This guide details how to compile the native C++ AVX2 shared library (`fastaudiocapture.dll`) and package the Java JAR artifact.

---

## Native Build Instructions

1. Open a PowerShell terminal in `FastAudioCapture` root.
2. Execute the native compilation script:
   ```cmd
   .\compile.bat
   ```
3. Package Maven uber-JAR:
   ```bash
   mvn clean package -DskipTests
   ```
