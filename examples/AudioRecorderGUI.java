import fastaudio.FastAudioCapture;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Audio Recorder GUI Example
 * 
 * Demonstrates FastAudioCapture API with a simple Swing interface.
 * Records from microphone and saves to WAV file.
 * 
 * Note: v1.0 uses stub implementation. Real WASAPI recording in v1.1.
 */
public class AudioRecorderGUI extends JFrame {
    
    private FastAudioCapture capture;
    private JLabel statusLabel;
    private JLabel levelLabel;
    private JProgressBar levelMeter;
    private JButton recordButton;
    private JButton stopButton;
    private JButton saveButton;
    private JComboBox<String> deviceCombo;
    private Timer levelTimer;
    
    public AudioRecorderGUI() {
        setTitle("FastAudioCapture - Recorder Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        
        initComponents();
        initAudio();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JLabel header = new JLabel("🎤 Audio Recorder", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(header, BorderLayout.NORTH);
        
        // Center panel
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 5, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        
        // Device selection
        JPanel devicePanel = new JPanel(new BorderLayout(5, 0));
        devicePanel.add(new JLabel("Device:"), BorderLayout.WEST);
        deviceCombo = new JComboBox<>();
        devicePanel.add(deviceCombo, BorderLayout.CENTER);
        centerPanel.add(devicePanel);
        
        // Level meter
        JPanel levelPanel = new JPanel(new BorderLayout(5, 0));
        levelLabel = new JLabel("Level:");
        levelPanel.add(levelLabel, BorderLayout.WEST);
        levelMeter = new JProgressBar(0, 100);
        levelMeter.setStringPainted(true);
        levelMeter.setValue(0);
        levelPanel.add(levelMeter, BorderLayout.CENTER);
        centerPanel.add(levelPanel);
        
        // Status
        statusLabel = new JLabel("Ready", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        centerPanel.add(statusLabel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        recordButton = new JButton("🔴 Record");
        recordButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        recordButton.setBackground(new Color(220, 53, 69));
        recordButton.setForeground(Color.WHITE);
        recordButton.setFocusPainted(false);
        recordButton.addActionListener(e -> startRecording());
        buttonPanel.add(recordButton);
        
        stopButton = new JButton("⏹ Stop");
        stopButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopRecording());
        buttonPanel.add(stopButton);
        
        saveButton = new JButton("💾 Save");
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveRecording());
        buttonPanel.add(saveButton);
        
        centerPanel.add(buttonPanel);
        add(centerPanel, BorderLayout.CENTER);
        
        // Footer
        JLabel footer = new JLabel("FastAudioCapture v1.0.0 - Stub Demo (Real audio in v1.1)", JLabel.CENTER);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footer.setForeground(Color.GRAY);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(footer, BorderLayout.SOUTH);
        
        // Level update timer
        levelTimer = new Timer(50, e -> updateLevel());
    }
    
    private void initAudio() {
        try {
            capture = new FastAudioCapture();
            
            // Load devices
            String[] devices = FastAudioCapture.getCaptureDevices();
            for (String device : devices) {
                deviceCombo.addItem(device);
            }
            
            statusLabel.setText("Ready - " + devices.length + " devices found");
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            recordButton.setEnabled(false);
        }
    }
    
    private void startRecording() {
        try {
            String device = (String) deviceCombo.getSelectedItem();
            boolean started = capture.startRecording(device, 44100, 2, 16);
            
            if (started) {
                statusLabel.setText("Recording...");
                recordButton.setEnabled(false);
                stopButton.setEnabled(true);
                saveButton.setEnabled(false);
                levelTimer.start();
            } else {
                statusLabel.setText("Failed to start recording");
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    private void stopRecording() {
        try {
            capture.stopRecording();
            levelTimer.stop();
            
            statusLabel.setText("Recording stopped");
            recordButton.setEnabled(true);
            stopButton.setEnabled(false);
            saveButton.setEnabled(true);
            levelMeter.setValue(0);
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    private void updateLevel() {
        float level = capture.getLevel();
        int percent = (int) (level * 100);
        levelMeter.setValue(percent);
        levelMeter.setString(percent + "%");
    }
    
    private void saveRecording() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("WAV files", "wav"));
        chooser.setSelectedFile(new File("recording.wav"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".wav")) {
                file = new File(file.getAbsolutePath() + ".wav");
            }
            
            try {
                if (capture.saveToFile(file.getAbsolutePath())) {
                    statusLabel.setText("Saved: " + file.getName());
                    saveButton.setEnabled(false);
                } else {
                    statusLabel.setText("Failed to save");
                }
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void dispose() {
        levelTimer.stop();
        if (capture != null) {
            capture.close();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AudioRecorderGUI().setVisible(true);
        });
    }
}
