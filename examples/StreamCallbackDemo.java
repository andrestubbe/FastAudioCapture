import fastaudio.FastAudioCapture;
import fastaudio.FastAudioCapture.AudioCallback;

/**
 * StreamCallbackDemo - Real-time audio streaming with callback
 * 
 * Shows professional audio callback usage for live processing.
 * 
 * Run: java --enable-native-access=ALL-UNNAMED -cp "fastaudiocapture.jar;." StreamCallbackDemo
 */
public class StreamCallbackDemo implements AudioCallback {
    
    private long sampleCount = 0;
    private float maxLevel = 0.0f;
    
    @Override
    public void onAudioData(short[] samples, long timestamp) {
        // Real-time audio processing!
        // This is called ~100 times per second from native thread
        
        sampleCount += samples.length;
        
        // Calculate peak level
        float peak = 0.0f;
        for (short s : samples) {
            float normalized = s / 32768.0f;
            if (Math.abs(normalized) > peak) {
                peak = Math.abs(normalized);
            }
        }
        if (peak > maxLevel) maxLevel = peak;
        
        // Real-time VU meter (console)
        int bars = (int) (peak * 50);
        StringBuilder meter = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            meter.append(i < bars ? "█" : "░");
        }
        System.out.printf("\r[%-50s] %5.1f%% | %d samples", meter.toString(), peak * 100, samples.length);
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  FastAudioCapture - Real-Time Stream Callback Demo          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("This demo shows professional real-time audio streaming.");
        System.out.println("The callback is invoked ~100x/sec from the native thread.");
        System.out.println();
        
        StreamCallbackDemo demo = new StreamCallbackDemo();
        FastAudioCapture capture = new FastAudioCapture();
        
        // Register callback BEFORE starting recording
        capture.setAudioCallback(demo);
        System.out.println("✓ Callback registered");
        
        // Start recording
        System.out.println("🎤 Starting 5-second recording...");
        System.out.println();
        
        capture.startRecording(48000, 2, 16);
        
        // Sleep while callback processes audio
        Thread.sleep(5000);
        
        capture.stopRecording();
        System.out.println();
        System.out.println();
        
        // Save to file
        capture.saveToFile("stream-recording.wav");
        System.out.println("✓ Saved to stream-recording.wav");
        
        // Statistics
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    STREAM STATISTICS                        ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.printf ("║  Total samples processed: %,d%n", demo.sampleCount);
        System.out.printf ("║  Peak level: %.1f%%%n", demo.maxLevel * 100);
        System.out.printf ("║  Duration: 5 seconds%n");
        System.out.printf ("║  Sample rate: 48kHz%n");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        capture.close();
    }
}
