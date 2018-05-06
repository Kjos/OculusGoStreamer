package net.kajos.Manager;

import net.kajos.Config;

public class Quality {
    public String frameFormat = Config.get().HIGH_FORMAT;
    public String interImageFormat = Config.get().INTERFRAME_FORMAT;
    public String lastKeyFrameFormat = frameFormat;
    public int frameSkip = 1;

    public float jpegQuality = Config.get().MAX_QUALITY;

    public void lower() {
        jpegQuality -= Config.get().QUALITY_ALPHA;
        if (jpegQuality < Config.get().MIN_QUALITY) {
            jpegQuality = Config.get().MIN_QUALITY;
            if (frameSkip < Config.get().MAX_FRAME_SKIP) frameSkip++;
        }
        frameFormat = Config.get().LOW_FORMAT;
    }

    public void raise() {
        if (frameSkip > 1) {
            frameSkip--;
        } else  {
            jpegQuality += Config.get().QUALITY_ALPHA;
            if (jpegQuality > Config.get().MAX_QUALITY) {
                jpegQuality = Config.get().MAX_QUALITY;
                frameFormat = Config.get().HIGH_FORMAT;
            }
        }
    }
}
