package fastaudiocapture.demo;
import fastaudiocapture.FastAudioCapture;

public class Demo {
    public static void main(String[] args) {
        System.out.println("--- FastAudioCapture 0.1.1 Demo ---");
        try {
            FastAudioCapture capture = new FastAudioCapture();
            System.out.println("Audio capture initialized: " + capture);
            System.out.println("✔ FastAudioCapture demo completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}