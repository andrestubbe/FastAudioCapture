import fastaudio.FastAudioCapture;

/**
 * Console Record Demo - Record from microphone with live level meter
 * 
 * Run: java --enable-native-access=ALL-UNNAMED -cp "fastaudiocapture.jar;." ConsoleRecordDemo
 */
public class ConsoleRecordDemo {
    
    public static void main(String[] args) throws Exception {
        int durationSeconds = args.length > 0 ? Integer.parseInt(args[0]) : 5;
        String outputFile = args.length > 1 ? args[1] : "my-recording.wav";
        
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     FastAudioCapture - Console Record Demo                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Duration: " + durationSeconds + " seconds");
        System.out.println("Output: " + outputFile);
        System.out.println();
        
        // Create capture
        FastAudioCapture capture = new FastAudioCapture();
        System.out.println("✓ Capture created");
        System.out.println();
        
        // List devices
        System.out.println("Available devices:");
        String[] devices = FastAudioCapture.getCaptureDevices();
        for (int i = 0; i < devices.length; i++) {
            System.out.println("  " + (i + 1) + ". " + devices[i]);
        }
        System.out.println();
        
        // Start recording
        System.out.println("┌────────────────────────────────────────────────────────┐");
        System.out.println("│  🎤 STARTING RECORDING - SPEAK NOW!                    │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println();
        
        boolean started = capture.startRecording(48000, 2, 16);
        if (!started) {
            System.err.println("❌ Failed to start recording!");
            capture.close();
            return;
        }
        
        // Record with live level meter
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < durationSeconds * 1000) {
            float level = capture.getLevel();
            int bars = (int) (level * 40);
            int remaining = durationSeconds - (int) ((System.currentTimeMillis() - startTime) / 1000);
            
            StringBuilder meter = new StringBuilder();
            for (int i = 0; i < 40; i++) {
                if (i < bars) {
                    meter.append("█");
                } else {
                    meter.append("░");
                }
            }
            
            System.out.printf("\r[%s] %5.1f%% | %ds remaining", 
                meter.toString(), level * 100, remaining);
            
            Thread.sleep(50);
        }
        
        System.out.println();
        System.out.println();
        
        // Stop recording
        System.out.println("Stopping recording...");
        capture.stopRecording();
        System.out.println("✓ Recording stopped");
        System.out.println();
        
        // Save to file
        System.out.println("Saving to " + outputFile + "...");
        boolean saved = capture.saveToFile(outputFile);
        if (saved) {
            System.out.println("✓ Saved successfully");
            
            // Show file info
            java.io.File f = new java.io.File(outputFile);
            System.out.printf("  Size: %,d bytes (%.1f MB)%n", 
                f.length(), f.length() / (1024.0 * 1024.0));
        } else {
            System.err.println("❌ Failed to save!");
        }
        
        capture.close();
        System.out.println("✓ Capture closed");
        System.out.println();
        
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  🎉 RECORDING COMPLETE!                                  ║");
        System.out.println("║                                                           ║");
        System.out.println("║  Play with:                                               ║");
        System.out.println("║    start " + outputFile);
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
