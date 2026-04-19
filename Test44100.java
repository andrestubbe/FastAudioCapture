import fastaudio.*;
public class Test44100 {
    public static void main(String[] args) throws Exception {
        FastAudioCapture capture = new FastAudioCapture();
        String device = FastAudioCapture.getDefaultDevice();
        System.out.println('Recording 5 sec from: ' + device);
        capture.startRecording(device, 44100, 2, 16);
        for (int i = 0; i < 50; i++) {
            System.out.print('.');
            Thread.sleep(100);
        }
        capture.stopRecording();
        if (capture.saveToFile('test_recording.wav')) {
            System.out.println('Saved!');
        }
        capture.close();
    }
}
