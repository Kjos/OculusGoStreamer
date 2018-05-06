package net.kajos.Manager;

import net.kajos.Config;
import net.kajos.LowPassFilter;

public class Quality {
    public String frameFormat = Config.HIGH_FORMAT;
    public String interImageFormat = Config.INTERFRAME_FORMAT;
    public String lastKeyFrameFormat = frameFormat;
    public int frameSkip = 1;

    public LowPassFilter jpegQuality = new LowPassFilter(Config.QUALITY_ALPHA, Config.MAX_QUALITY);

    public void lower() {
        if (jpegQuality.lower(Config.QUALITY_ADJUST, Config.MIN_QUALITY)) {
            if (frameSkip < Config.MAX_FRAME_SKIP) frameSkip++;
        }
        frameFormat = Config.LOW_FORMAT;
    }

    public void raise() {
        if (jpegQuality.raise(Config.QUALITY_ADJUST, Config.MAX_QUALITY)) {
            frameFormat = Config.HIGH_FORMAT;
            if (frameSkip > 1) frameSkip--;
        }
    }
}
