package net.kajos.Manager;

import net.kajos.Config;
import net.kajos.LowPassFilter;

public class Quality {
    public String frameFormat = Config.get().HIGH_FORMAT;
    public String interImageFormat = Config.get().INTERFRAME_FORMAT;
    public String lastKeyFrameFormat = frameFormat;
    public int frameSkip = 1;

    public LowPassFilter jpegQuality = new LowPassFilter(Config.get().QUALITY_ALPHA, Config.get().MAX_QUALITY);

    public void lower() {
        if (jpegQuality.lower(Config.get().MIN_QUALITY)) {
            if (frameSkip < Config.get().MAX_FRAME_SKIP) frameSkip++;
        }
        frameFormat = Config.get().LOW_FORMAT;
    }

    public void raise() {
        if (frameSkip > 1) {
            frameSkip--;
        } else  if (jpegQuality.raise(Config.get().MAX_QUALITY)) {
            frameFormat = Config.get().HIGH_FORMAT;
        }
    }
}
