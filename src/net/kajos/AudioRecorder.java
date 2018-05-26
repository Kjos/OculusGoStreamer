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
    //rate * channels * sampleSize / 1000 * fps * [rounding];

    private AudioFormat format;
    private int payloadSize;

    private void createAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        int rate = 44100;
        int channels = 1;
        int sampleSize = 8;
        boolean bigEndian = true;
        int bytesPerSample = sampleSize / 8 * channels;

        payloadSize = bytesPerSample * rate / 30 / 2 * 2;

        format = new AudioFormat(encoding, rate, sampleSize, channels, bytesPerSample
                , rate, bigEndian);
    }

    public AudioRecorder(AudioManager manager) {
        this.manager = manager;

        try {
            createAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Audio line not supported");
                return;
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
