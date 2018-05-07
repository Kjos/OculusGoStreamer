package net.kajos;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;

public class Config {
    private static Config instance = null;

    public static Config get() {
        return instance;
    }

    private static JSONObject createDefaultConfig() {
        JSONObject obj = new JSONObject();
        obj.put("WEB_PORT", instance.WEB_PORT);

        obj.put("SCREEN_WIDTH", instance.SCREEN_WIDTH);
        obj.put("SCREEN_HEIGHT", instance.SCREEN_HEIGHT);
        obj.put("SCREEN_LEFT", instance.SCREEN_LEFT);
        obj.put("SCREEN_TOP", instance.SCREEN_TOP);
        obj.put("FPS", instance.FPS);
        obj.put("ADD_FRAMES_LATENCY", instance.ADD_FRAMES_LATENCY);
        obj.put("MAX_FRAME_SKIP", instance.MAX_FRAME_SKIP);

        obj.put("MIN_QUALITY", instance.MIN_QUALITY);
        obj.put("MAX_QUALITY", instance.MAX_QUALITY);
        obj.put("QUALITY_ALPHA", instance.QUALITY_ALPHA);

        obj.put("HIGH_FORMAT", instance.HIGH_FORMAT);
        obj.put("LOW_FORMAT", instance.LOW_FORMAT);
        obj.put("INTERFRAME_FORMAT", instance.INTERFRAME_FORMAT );

        obj.put("KEYFRAME_THRESHOLD", instance.KEYFRAME_THRESHOLD);
        obj.put("KEYFRAME_THRESHOLD_SUM", instance.KEYFRAME_THRESHOLD_SUM);
        return obj;
    }

    public static Config load() {
        instance = new Config();

        File file = new File("config.json");
        JSONObject configJson = null;
        if (!file.exists() || !file.canRead()) {
            Config.print("No config.json file found!");
            Config.print("Writing default config.json.");
            try {
                file.createNewFile();

                configJson = createDefaultConfig();
                try (PrintWriter out = new PrintWriter(file)) {
                    out.println(configJson.toString(1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            try {
                String contents = new String(Files.readAllBytes(file.toPath()));
                configJson = new JSONObject(contents);

                instance.WEB_PORT = configJson.getInt("WEB_PORT");

                instance.SCREEN_WIDTH = configJson.getInt("SCREEN_WIDTH");
                instance.SCREEN_HEIGHT = configJson.getInt("SCREEN_HEIGHT");
                instance.SCREEN_LEFT = configJson.getInt("SCREEN_LEFT");
                instance.SCREEN_TOP = configJson.getInt("SCREEN_TOP");
                instance.FPS = configJson.getInt("FPS");
                instance.ADD_FRAMES_LATENCY = configJson.getInt("ADD_FRAMES_LATENCY");
                instance.MAX_FRAME_SKIP = configJson.getInt("MAX_FRAME_SKIP");

                instance.MIN_QUALITY = configJson.getFloat("MIN_QUALITY");
                instance.MAX_QUALITY = configJson.getFloat("MAX_QUALITY");
                instance.QUALITY_ALPHA = configJson.getFloat("QUALITY_ALPHA")
                        / instance.FPS;

                instance.HIGH_FORMAT = configJson.getString("HIGH_FORMAT");
                instance.LOW_FORMAT = configJson.getString("LOW_FORMAT");
                instance.INTERFRAME_FORMAT = configJson.getString("INTERFRAME_FORMAT");

                instance.KEYFRAME_THRESHOLD = configJson.getFloat("KEYFRAME_THRESHOLD");
                instance.KEYFRAME_THRESHOLD_SUM = configJson.getFloat("KEYFRAME_THRESHOLD_SUM");
            } catch (Exception e) {
                Config.print("Error reading config.json!");
                Config.print("Remove the config.json and a default config.json will be generated on run.");
                e.printStackTrace();
                System.exit(1);
            }
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
    public int FPS = 30;

    public int ADD_FRAMES_LATENCY = 1;

    public float QUALITY_ALPHA = 1f;
    public float KEYFRAME_THRESHOLD = 0.03f;
    public float KEYFRAME_THRESHOLD_SUM = 0.16f;

    public String HIGH_FORMAT = Constants.PNG;
    public String LOW_FORMAT = Constants.JPEG;
    public String INTERFRAME_FORMAT = Constants.JPEG;

    // 1 is no frameskip, 2 every other frame, 3 every 2 in 3 frames are skipped
    public int MAX_FRAME_SKIP = 3;

    public float MIN_QUALITY = .3f;
    public float MAX_QUALITY = .9f;
}
