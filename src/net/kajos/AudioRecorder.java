package net.kajos;

import net.kajos.Manager.AudioManager;

import javax.sound.sampled.*;
import java.io.IOException;

public class AudioRecorder {

    private AudioFormat format;
    private int payloadSize;

    private void createAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        int rate = 44100;
        int channels = 2;
        int sampleSize = 8;
        boolean bigEndian = true;
        int bytesPerSample = sampleSize / 8 * channels;

        payloadSize = rate * bytesPerSample / 30 / 2 * 2;

        format = new AudioFormat(encoding, rate, sampleSize, channels, bytesPerSample
                , rate, bigEndian);
    }

    public AudioRecorder(AudioManager manager) {

        try {
            createAudioFormat();

            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();    //get available mixers
            System.out.println("Available mixers:");
            int sel = -1;
            for (int i = 0; i < mixerInfo.length; i++) {
                try {
                    AudioSystem.getTargetDataLine(format, mixerInfo[i]);
                    System.out.println("Mixer " + i + ": " + mixerInfo[i].getName());
                    sel = i;
                } catch (IllegalArgumentException ex) {

                }
            }

            if (sel == -1) {
                System.out.println("No suitable audio line found!");
                return;
            }
            TargetDataLine line = AudioSystem.getTargetDataLine(format);
            //line = AudioSystem.getTargetDataLine(format, mixerInfo[sel]);
            //System.out.println("Selected mixer: " + sel);

            line.open(format);
            line.start();

            AudioInputStream ais = new AudioInputStream(line);

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
