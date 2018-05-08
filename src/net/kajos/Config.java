package net.kajos;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

public class Config {
    private static Config instance = null;

    public static Config get() {
        return instance;
    }

    private static Rectangle getMaximumScreenBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for(GraphicsDevice curGs : gs)
        {
            GraphicsConfiguration[] gc = curGs.getConfigurations();
            for(GraphicsConfiguration curGc : gc)
            {
                Rectangle bounds = curGc.getBounds();
                return bounds;
            }
        }
        return null;
    }

    private static JSONObject createDefaultConfig() {
        JSONObject obj = new JSONObject();
        obj.put("WEB_PORT", instance.WEB_PORT);

        Rectangle rect = getMaximumScreenBounds();
        if (rect != null) {
            System.out.println("Using screen as default:");
            System.out.println("> Width: " + rect.width + ", height: " + rect.height);
            System.out.println("> Left: " + rect.x + ", top: " + rect.y);
            obj.put("SCREEN_WIDTH", rect.width);
            obj.put("SCREEN_HEIGHT", rect.height);
            obj.put("SCREEN_LEFT", rect.x);
            obj.put("SCREEN_TOP", rect.y);
        } else {
            System.out.println("Error: Couldn't determine screen size!");
            System.out.println("Edit SCREEN_WIDTH and SCREEN_HEIGHT to match your display.");
            obj.put("SCREEN_WIDTH", instance.SCREEN_WIDTH);
            obj.put("SCREEN_HEIGHT", instance.SCREEN_HEIGHT);
            obj.put("SCREEN_LEFT", instance.SCREEN_LEFT);
            obj.put("SCREEN_TOP", instance.SCREEN_TOP);
        }
        System.out.println();

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

        JSONObject configJson = null;
        try {
            File file = new File("config.json");
            if (!file.exists() || !file.canRead()) {
                Config.print("No config.json file found!");
                Config.print("Writing default config.json.");
                    file.createNewFile();

                    configJson = createDefaultConfig();
                    try (PrintWriter out = new PrintWriter(file)) {
                        out.println(configJson.toString(1));
                    }
            } else {
                String contents = new String(Files.readAllBytes(file.toPath()));
                configJson = new JSONObject(contents);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
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
            Config.print("Error: config.json is malformed!");
            Config.print("Remove the config.json and a default config.json will be generated on run.");
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
