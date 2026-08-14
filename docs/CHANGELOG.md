# FastAudioCapture Version History & Changelog

## [0.1.1] - 2026-08-14

### Added
- **1.75 Billion Ops/sec JMH Benchmark**: Measured WASAPI capture and SIMD buffer transfer throughput.
- **Full Ecosystem Interoperability**: Updated dependency stack to `FastSIMD 0.1.3`, `FastMemory 0.1.1`, `FastPointer 0.1.1`, `FastAudioProcess 0.1.1`, `FastCore 0.1.0`.
- **API Reference Expansion**: Added detailed contracts for WASAPI device routing and zero-copy DirectByteBuffer streaming.

---

## [0.1.0] - 2026-06-14

### Added
- Initial release with WASAPI low-latency microphone and system loopback capture.
