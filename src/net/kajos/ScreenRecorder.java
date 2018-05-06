package net.kajos;

import net.kajos.Manager.Manager;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenRecorder {
    private Manager manager;

    public ScreenRecorder(Manager manager) {
        this.manager = manager;
    }

    private BufferedImage image;
    private MediaPlayerFactory factory;
    private DirectMediaPlayer mediaPlayer;
    private RenderCallback callback;

    public int videoWidth, videoHeight;
    public void start(int width, int height) {
        this.videoWidth = width;
        this.videoHeight = height;

        image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration().
                createCompatibleImage(videoWidth, videoHeight);
        image.setAccelerationPriority(1.0f);

        String mrl = "screen://";
        String[] options = {
                ":screen-fps=" + Config.get().FPS,
                ":live-caching=0",
                ":screen-width=" + Config.get().SCREEN_WIDTH,
                ":screen-height=" + Config.get().SCREEN_HEIGHT,
                ":screen-left=" + Config.get().SCREEN_LEFT,
                ":screen-top=" + Config.get().SCREEN_TOP
        };
        factory = new MediaPlayerFactory();
        callback = new RenderCallback(image, manager);
        mediaPlayer = factory.newDirectMediaPlayer(new TestBufferFormatCallback(), callback);
        mediaPlayer.playMedia(mrl, options);

        System.out.println("Recorder set up, width: " + width + ", height: " + height);
    }

    public void stop() {
        if (mediaPlayer != null) mediaPlayer.stop();
    }

    private final class TestBufferFormatCallback implements BufferFormatCallback {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            return new RV32BufferFormat(videoWidth, videoHeight);
        }

    }

}
