import fastaudio.FastAudioCapture;
import fastaudio.FastAudioPlayer;

import javax.swing.*;
import java.awt.*;

/**
 * Combined Record + Playback Test
 * 
 * Records 5 seconds from microphone, then plays it back.
 * Real WASAPI test - proves both APIs work together.
 */
public class TestRecordPlay {
    
    public static void main(String[] args) {
        try {
            // Set native access (Java 17+)
            System.setProperty("jdk.module.illegalNativeAccess", "permit");
            
            System.out.println("=== FastAudio Real WASAPI Test ===\n");
            
            // Show available devices
            System.out.println("1. Available Capture Devices:");
            String[] captureDevices = FastAudioCapture.getCaptureDevices();
            for (String device : captureDevices) {
                System.out.println("   - " + device);
            }
            
            System.out.println("\n2. Available Playback Devices:");
            String[] playDevices = FastAudioPlayer.getDevices();
            for (String device : playDevices) {
                System.out.println("   - " + device);
            }
            
            // Create capture
            System.out.println("\n3. Creating Audio Capture...");
            FastAudioCapture capture = new FastAudioCapture();
            System.out.println("   ✓ Capture created");
            
            // Get default device
            String defaultDevice = FastAudioCapture.getDefaultDevice();
            System.out.println("\n4. Using device: " + defaultDevice);
            
            // Start recording
            System.out.println("\n5. Starting 5-second recording...");
            System.out.println("   (Speak into your microphone now!)");
            
            boolean started = capture.startRecording(defaultDevice, 44100, 2, 16);
            if (!started) {
                System.out.println("   ✗ Failed to start recording");
                return;
            }
            
            // Monitor levels for 5 seconds
            for (int i = 0; i < 50; i++) {
                float level = capture.getLevel();
                int bars = (int)(level * 40);
                String meter = "█".repeat(bars) + "░".repeat(40 - bars);
                System.out.printf("\r   %s %.1f%%", meter, level * 100);
                Thread.sleep(100);
            }
            
            // Stop recording
            capture.stopRecording();
            System.out.println("\n   ✓ Recording stopped");
            
            // Save to file
            String filename = "test_recording.wav";
            System.out.println("\n6. Saving to: " + filename);
            if (capture.saveToFile(filename)) {
                System.out.println("   ✓ Saved successfully");
            } else {
                System.out.println("   ✗ Failed to save");
                return;
            }
            
            // Playback
            System.out.println("\n7. Playing back recording...");
            FastAudioPlayer player = new FastAudioPlayer();
            
            if (player.load(filename)) {
                long duration = player.getDuration();
                System.out.println("   Duration: " + (duration / 1000.0) + " seconds");
                
                System.out.println("   Playing...");
                player.play();
                
                // Wait for playback
                while (player.isPlaying()) {
                    long pos = player.getPosition();
                    int percent = (int)((pos * 100) / duration);
                    String progress = "█".repeat(percent / 2) + "░".repeat(50 - percent / 2);
                    System.out.printf("\r   %s %d%%", progress, percent);
                    Thread.sleep(100);
                }
                System.out.println("\n   ✓ Playback finished");
            } else {
                System.out.println("   ✗ Failed to load recording");
            }
            
            // Cleanup
            capture.close();
            player.close();
            
            System.out.println("\n=== Test Complete! ===");
            System.out.println("Real WASAPI audio capture and playback working!");
            
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
