package net.kajos;

import net.kajos.Manager.Manager;
import net.kajos.Manager.Viewer;
import net.kajos.Manager.Quality;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RenderCallback extends RenderCallbackAdapter {
    private int frameCount = 0;

    private ExecutorService exec;
    private ImagePool pool;

    private int width, height;
    private Manager manager;

    public RenderCallback(BufferedImage image, Manager manager) {
        super(((DataBufferInt) image.getRaster().getDataBuffer()).getData());
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.manager = manager;

        exec = Executors.newFixedThreadPool(Constants.THREADS);

        // Interlaced, so height divided by two
        pool = new ImagePool(width,
                height / 2, BufferedImage.TYPE_INT_RGB);

    }

    private boolean missingKeyframe(Viewer viewer) {
        return viewer.rgb == null || viewer.rgb[0][0][0].length != width ||
                viewer.rgb[0][0].length != height;
    }

    @Override
    public void onDisplay(DirectMediaPlayer mediaPlayer, int[] frameData) {
        frameCount++;

        printBytes();

        Viewer viewer = manager.getViewer();
        if (viewer == null) return;

        // Initial latency polling
        if (viewer.latencyPolls < Constants.LATENCY_POLL_FRAMES) {
            manager.sendEmptyImage(frameCount);

            if (viewer.receivedFrameStamp == 0) return;

            int latency = frameCount - viewer.receivedFrameStamp;

            if (latency < 0) return;

            viewer.latencyPolls++;

            viewer.allowedLatency += latency;

            if (viewer.latencyPolls == Constants.LATENCY_POLL_FRAMES) {
                viewer.allowedLatency /= Constants.LATENCY_POLL_FRAMES;
                viewer.allowedLatency += Config.get().ADD_FRAMES_LATENCY;
                System.out.println("Polled achievable latency: " + viewer.allowedLatency);
            }
            return;
        }

        final int frameId = frameCount;
        exec.submit(new Runnable() {

            private void runFrame() {
                boolean isKeyFrame = viewer.frameCount == 0;
                boolean missingKeyFrame = missingKeyframe(viewer);

                byte[] data;
                if (isKeyFrame || missingKeyFrame) {
                    data = interlaceKeyFrame(viewer, frameId, frameData, missingKeyFrame);
                } else {
                    data = interlaceFrame(viewer, frameId, frameData);
                }

                viewer.frameSem.release();

                if (data == null) {
                    manager.sendEmptyImage(frameId);
                } else {
                    manager.sendData(data);
                }
            }

            @Override
            public void run() {
                boolean latencyTooHigh = frameId - viewer.receivedFrameStamp > viewer.allowedLatency;
                if (latencyTooHigh) {
                    System.out.println("Latency too high! Lowering quality");
                }

                Quality quality = viewer.quality;
                if (latencyTooHigh) {
                    quality.lower();
                } else {
                    quality.raise();
                }

                boolean lateEncoding = !viewer.frameSem.tryAcquire();

                if (lateEncoding) {
                    manager.sendEmptyImage(frameId);
                    System.out.println("Busy encoding");

                } else if(frameId % viewer.quality.frameSkip != 0) {
                    manager.sendEmptyImage(frameId);
                    System.out.println("Skipped frame");
                    viewer.frameSem.release();

                } else {
                    runFrame();
                }
            }
        });
    }

    private static int frameSum = 0;
    private static int bytesSum = 0;
    private static long countTime = 0;
    private static void printBytes() {
        long time = System.currentTimeMillis();
        long diff = time - countTime;
        if (diff > 1000) {
            System.out.println((bytesSum * 8 / 1024 / 1024) + "Mbit/s, " +
                    frameSum + "FPS");
            bytesSum = 0;
            frameSum = 0;
            countTime = time;
        }
    }

    private byte[] interlaceKeyFrame(Viewer viewer, int frameStamp, int[] frameData, boolean missingKeyFrame) {
        Quality quality = viewer.quality;

        viewer.keyFrameToggle = !viewer.keyFrameToggle;

        boolean interpol = viewer.keyFrameToggle;
        int keyframe = interpol ? 0 : 1;
        int code = interpol ? 1 : 2;

        ImageWrapper img = pool.get();

        int startY = 0;
        if (interpol) startY += 1;

        int p = startY * width;
        int p2 = 0;

        int[] pixels = img.pixels;

        if (missingKeyFrame) {
            viewer.rgb = new int[2][3][height][width];
            System.out.println("Create framebuffer");
        }

        int[][] r = viewer.rgb[keyframe][0];
        int[][] g = viewer.rgb[keyframe][1];
        int[][] b = viewer.rgb[keyframe][2];

        for (int y = 0; y < img.height; y++, p+=width) {
            int[] ar = r[y];
            int[] ag = g[y];
            int[] ab = b[y];
            for (int x = 0; x < img.width; x++, p++, p2++) {
                int c = frameData[p];
                int cr = (c >> 16) & 0xff;
                int cg = (c >> 8) & 0xff;
                int cb = c & 0xff;

                ar[x] = cr;
                ag[x] = cg;
                ab[x] = cb;
                pixels[p2] = c;
            }
        }

        viewer.lastDifference[keyframe] = 0;
        viewer.sumDifference = 0f;
        viewer.lastInterFrameSize[keyframe] = 0;

        byte[] data = img.getCompressedBytes(code, frameStamp, quality.jpegQuality,
                quality.frameFormat);

        bytesSum += data.length;
        frameSum++;

        if (Constants.FRAME_LOG) {
            System.out.println("Keyframe " + keyframe + ": " + quality.frameFormat + ", size: " + data.length +
                    ", quality: " + quality.jpegQuality);
        }

        viewer.frameCount++;
        viewer.lastKeyFrameSize[keyframe] = data.length;

        pool.put(img);

        return data;
    }

    private byte[] interlaceFrame(Viewer viewer, int frameStamp, int[] frameData) {
        byte[] data = null;

        Quality quality = viewer.quality;

        int ip = viewer.frameCount % 2;
        boolean interpol = (ip == 1) ^ viewer.keyFrameToggle;
        int keyframe = interpol ? 0 : 1;
        int code = interpol ? 3 : 4;

        ImageWrapper img = pool.get();

        int startY = 0;
        if (interpol) startY += 1;

        int p = startY * width;
        int p2 = 0;

        int[] pixels = img.pixels;

        viewer.frameCount++;

        float difference = 0;

        int[][] ar = viewer.rgb[keyframe][0];
        int[][] ag = viewer.rgb[keyframe][1];
        int[][] ab = viewer.rgb[keyframe][2];

        for (int y = 0; y < img.height; y++, p += width) {
            int[] r = ar[y];
            int[] g = ag[y];
            int[] b = ab[y];
            for (int x = 0; x < img.width; x++, p++, p2++) {
                int c = frameData[p];
                int cr = (c >> 16) & 0xff;
                int cg = (c >> 8) & 0xff;
                int cb = c & 0xff;

                cr = cr - r[x];
                cg = cg - g[x];
                cb = cb - b[x];

                difference += Math.abs(cr);
                difference += Math.abs(cg);
                difference += Math.abs(cb);

                cr /= 2;
                cg /= 2;
                cb /= 2;

                cr = 127 + cr;
                cg = 127 + cg;
                cb = 127 + cb;

                pixels[p2] = (cr << 16) | (cg << 8) | cb;
            }
        }

        difference /= (float)(img.width * img.height * 255);
        difference /= (float)Config.get().FPS;

        float diff = Math.abs(viewer.lastDifference[keyframe] - difference);

        viewer.sumDifference += difference;

        if (diff > Constants.IGNORE_DIFFERENCE / (float)Config.get().FPS) {

            data = img.getCompressedBytes(code, frameStamp, quality.jpegQuality,
                    quality.interImageFormat);

            bytesSum += data.length;
            frameSum++;

            if (Constants.FRAME_LOG) {
                System.out.println("Interframe " + keyframe + ": " + quality.interImageFormat + ", size: " + data.length +
                        ", diff.:" + diff + ", sum diff.:" + viewer.sumDifference + ", quality: " + quality.jpegQuality);
            }

            // Frame is not going back to keyframe
            if (data.length > viewer.lastKeyFrameSize[keyframe]) {
                if (Constants.FRAME_LOG) System.out.println("Bframes: " + viewer.frameCount);
                viewer.frameCount = 0;
            } else if (viewer.lastDifference[keyframe] < difference &&
                    viewer.lastInterFrameSize[keyframe] < data.length) {

                if (difference > Config.get().KEYFRAME_THRESHOLD ||
                        data.length > viewer.lastKeyFrameSize[keyframe]) {
                    if (Constants.FRAME_LOG) System.out.println("Bframes: " + viewer.frameCount);
                    viewer.frameCount = 0;
                }
            }

            viewer.lastInterFrameSize[keyframe] = data.length;
        }

        if (viewer.sumDifference > Config.get().KEYFRAME_THRESHOLD_SUM) {
            if (Constants.FRAME_LOG)
                System.out.println("Bframes: " + viewer.frameCount + " Sum diff: " +
                        viewer.sumDifference);
            viewer.frameCount = 0;
        }

        viewer.lastDifference[keyframe] = difference;

        pool.put(img);

        return data;
    }
}
