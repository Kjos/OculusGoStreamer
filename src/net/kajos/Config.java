package net.kajos;

import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;

public class Config {
    private static Config instance = null;

    public static Config get() {
        return instance;
    }

    private static String createDefaultConfig() {
        JSONObject obj = new JSONObject();
        obj.put("WEB_PORT", instance.WEB_PORT);

        obj.put("SCREEN_WIDTH", instance.SCREEN_WIDTH);
        obj.put("SCREEN_HEIGHT", instance.SCREEN_HEIGHT);
        obj.put("SCREEN_LEFT", instance.SCREEN_LEFT);
        obj.put("SCREEN_TOP", instance.SCREEN_TOP);
        obj.put("FPS", instance.FPS);
        obj.put("MAX_FRAMES_LATENCY", instance.MAX_FRAMES_LATENCY);
        obj.put("MAX_FRAME_SKIP", instance.MAX_FRAME_SKIP);

        obj.put("MIN_QUALITY", instance.MIN_QUALITY);
        obj.put("MAX_QUALITY", instance.MAX_QUALITY);
        obj.put("QUALITY_ALPHA", instance.QUALITY_ALPHA);

        obj.put("HIGH_FORMAT", instance.HIGH_FORMAT);
        obj.put("LOW_FORMAT", instance.LOW_FORMAT);
        obj.put("INTERFRAME_FORMAT", instance.INTERFRAME_FORMAT );

        return obj.toString(1);
    }

    public static Config load() {
        instance = new Config();

        File file = new File("config.json");
        if (!file.exists() || !file.canRead()) {
            Config.print("No config.json file found!");
            Config.print("Writing default config.json.");
            try {
                file.createNewFile();
                try (PrintWriter out = new PrintWriter(file)) {
                    out.println(createDefaultConfig());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        try {
            String contents = new String(Files.readAllBytes(file.toPath()));
            JSONObject obj = new JSONObject(contents);
            instance.WEB_PORT = obj.getInt("WEB_PORT");

            instance.SCREEN_WIDTH = obj.getInt("SCREEN_WIDTH");
            instance.SCREEN_HEIGHT = obj.getInt("SCREEN_HEIGHT");
            instance.SCREEN_LEFT = obj.getInt("SCREEN_LEFT");
            instance.SCREEN_TOP = obj.getInt("SCREEN_TOP");
            instance.FPS = obj.getInt("FPS");
            instance.MAX_FRAMES_LATENCY = obj.getInt("MAX_FRAMES_LATENCY");
            instance.MAX_FRAME_SKIP = obj.getInt("MAX_FRAME_SKIP");

            instance.MIN_QUALITY = obj.getFloat("MIN_QUALITY");
            instance.MAX_QUALITY = obj.getFloat("MAX_QUALITY");
            instance.QUALITY_ALPHA = obj.getFloat("QUALITY_ALPHA");

            instance.HIGH_FORMAT = obj.getString("HIGH_FORMAT");
            instance.LOW_FORMAT = obj.getString("LOW_FORMAT");
            instance.INTERFRAME_FORMAT = obj.getString("INTERFRAME_FORMAT");

        } catch (Exception e) {
            Config.print("Error reading config.json!");
            e.printStackTrace();
            System.exit(1);
        }
        return instance;
    }

    public static void print(String line) {
        System.out.println(line);
    }

    public int WEB_PORT = 7578;

    public int SCREEN_LEFT = 0;
    public int SCREEN_TOP = 0;
    public int SCREEN_WIDTH = 1920;
    public int SCREEN_HEIGHT = 1080;
    public int FPS = 20;

    public int MAX_FRAMES_LATENCY = 3;

    public float QUALITY_ALPHA = .1f;
    public float KEYFRAME_THRESHOLD = .3f;
    public float KEYFRAME_THRESHOLD2 = 3.3f;

    public String HIGH_FORMAT = Constants.PNG;
    public String LOW_FORMAT = Constants.JPEG;
    public String INTERFRAME_FORMAT = Constants.JPEG;

    public float IGNORE_DIFFERENCE = 0.000005f;

    // 1 is no frameskip, 2 every other frame, 3 every 2 in 3 frames are skipped
    public int MAX_FRAME_SKIP = 3;

    public float MIN_QUALITY = .3f;
    public float MAX_QUALITY = 1f;
}
