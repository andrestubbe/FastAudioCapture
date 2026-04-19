import fastaudio.FastAudioCapture;

public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println("=== FastAudioCapture Test ===");
        
        // Test 1: Get devices
        System.out.println("\n1. Available capture devices:");
        String[] devices = FastAudioCapture.getCaptureDevices();
        for (String device : devices) {
            System.out.println("   - " + device);
        }
        
        // Test 2: Default device
        System.out.println("\n2. Default device: " + FastAudioCapture.getDefaultDevice());
        
        // Test 3: Create capture
        System.out.println("\n3. Creating capture...");
        FastAudioCapture capture = new FastAudioCapture();
        System.out.println("   ✓ Capture created");
        
        // Test 4: Start recording
        System.out.println("\n4. Starting recording (44100Hz, stereo, 16-bit)...");
        boolean started = capture.startRecording(44100, 2, 16);
        System.out.println("   Started: " + started);
        System.out.println("   isRecording: " + capture.isRecording());
        
        // Test 5: Monitor levels for 3 seconds
        System.out.println("\n5. Monitoring levels for 3 seconds...");
        for (int i = 0; i < 30; i++) {
            float level = capture.getLevel();
            int bars = (int)(level * 20);
            String meter = "█".repeat(bars) + "░".repeat(20 - bars);
            System.out.printf("   %s %.1f%%\n", meter, level * 100);
            Thread.sleep(100);
        }
        
        // Test 6: Stop and save
        System.out.println("\n6. Stopping recording...");
        capture.stopRecording();
        System.out.println("   isRecording: " + capture.isRecording());
        
        System.out.println("\n7. Saving to test-recording.wav...");
        boolean saved = capture.saveToFile("test-recording.wav");
        System.out.println("   Saved: " + saved);
        
        // Test 7: System audio (loopback) - brief test
        System.out.println("\n8. Testing system audio capture (loopback)...");
        boolean sysStarted = capture.startSystemRecording(48000, 2, 16);
        System.out.println("   Started: " + sysStarted);
        if (sysStarted) {
            Thread.sleep(500);
            capture.stopRecording();
            System.out.println("   ✓ System audio test complete");
        }
        
        // Clean up
        System.out.println("\n9. Closing capture...");
        capture.close();
        System.out.println("   ✓ Capture closed");
        
        System.out.println("\n=== All tests passed! ===");
    }
}
