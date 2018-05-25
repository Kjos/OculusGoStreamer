package net.kajos;

import net.kajos.Manager.AudioManager;
import net.kajos.Manager.Manager;

import javax.sound.sampled.*;
import java.io.IOException;

public class AudioRecorder {
    private TargetDataLine line;
    private AudioInputStream ais;

    private AudioManager manager;

    /**
     * Defines an audio format
     */
    private AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    private int rate = 44100;
    private int channels = 1;
    private int sampleSize = 8;
    private boolean bigEndian = true;
    private int bytesPerSample = sampleSize / 8 * channels;
    private int payloadSize = bytesPerSample * rate / 33;//rate * channels * sampleSize;

    private AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, bytesPerSample
            * channels, rate, bigEndian);

    public AudioRecorder(AudioManager manager) {
        this.manager = manager;

        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();	// start capturing

            ais = new AudioInputStream(line);

            System.out.println("Start capturing sound...");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[payloadSize*2];
                    int count = 0;
                    while (true) {
                        try {
                            int len = ais.read(buffer, count, payloadSize);
                            count+=len;
                            if (count > payloadSize) {
                                manager.sendData(buffer, payloadSize);
                                for (int i = payloadSize, k = 0; i < count; i++, k++) {
                                    buffer[k] = buffer[i];
                                }
                                count -= payloadSize;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
}
