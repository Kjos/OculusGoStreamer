package net.kajos;

public class Config {
    public static final int WEB_PORT = 7578;

    public static final int SCREEN_LEFT = 1920;
    public static final int SCREEN_TOP = 0;
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    public static final int FPS = 30;

    public static final int MAX_BANDWIDTH_BYTES_FRAME = 30*1024*1024/8/Config.FPS;

    public static final int MAX_FRAMES_LATENCY = 2;

    public static float FRAMETIME_ALPHA = 0.1f;
    public static float QUALITY_ALPHA = 1f;
    public static float QUALITY_ADJUST = .05f;
    public static float KEYFRAME_THRESHOLD = .3f;
    public static float KEYFRAME_THRESHOLD2 = 3.3f;

    public static final float LOWPASS_BANDWIDTH = 0.1f;

    public static final String HIGH_FORMAT = Constants.PNG;
    public static final String LOW_FORMAT = Constants.JPEG;
    public static final String INTERFRAME_FORMAT = Constants.JPEG;

    public static final float IGNORE_DIFFERENCE = 0.000005f;

    // 1 is no frameskip, 2 every other frame, 3 every 2 in 3 frames are skipped
    public static final int MAX_FRAME_SKIP = 3;

    public static final float MIN_QUALITY = .3f;
    public static final float MAX_QUALITY = .9f;
}
