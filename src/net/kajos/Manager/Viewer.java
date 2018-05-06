package net.kajos.Manager;


import net.kajos.Config;
import net.kajos.LowPassFilter;

import java.util.concurrent.Semaphore;

public class Viewer {
    public Quality quality = new Quality();

    public int rgb[][][] = null;

    public int frameTime = 1000 / Config.FPS;

    public int lastKeyFrameSize = -1;
    public int lastInterFrameSize = 0;
    public boolean skipInterlace2 = false;
    public float lastDifference = 0;
    public int frameCount = 0;
    public Semaphore frameSem = new Semaphore(1);
    public float sumDifference = 0f;
    public boolean keyFrameToggle = true;

    public int clientWidth = 1000;
    public int clientHeight = 1000;

    public int receivedFrameStamp = 0;

    public void frameUpdate(int frameStamp) {
        receivedFrameStamp = frameStamp;
    }
}
