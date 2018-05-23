package net.kajos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;

public class Config {
    private static Config instance = null;

    public static Config get() {
        return instance;
    }

    private static JSONObject createDefaultConfig() {
        JSONObject obj = new JSONObject();
        obj.put("WEB_PORT", instance.WEB_PORT);

        ArrayList<Screen> screens = Screen.getScreens();
        if (screens.size() > 0) {
            System.out.println("Found " + screens.size() + " screens.");

            JSONArray displays = new JSONArray();
            instance.SCREENS = new Screen[screens.size()];

            for (int i = 0; i < screens.size(); i++) {
                Screen screen = screens.get(i);
                instance.SCREENS[i] = screen;

                JSONObject display = new JSONObject();
                display.put("SCREEN_X", screen.x);
                display.put("SCREEN_Y", screen.y);
                display.put("SCREEN_WIDTH", screen.width);
                display.put("SCREEN_HEIGHT", screen.height);
                displays.put(display);
            }

            obj.put("SCREENS", displays);


        } else {
            System.out.println("Error: Couldn't determine screen size!");
            System.out.println("Edit SCREEN_WIDTH and SCREEN_HEIGHT to match your display.");
            Screen screen = new Screen(0, 0, 1920, 1080);

            JSONArray displays = new JSONArray();
            JSONObject display = new JSONObject();
            display.put("SCREEN_X", screen.x);
            display.put("SCREEN_Y", screen.y);
            display.put("SCREEN_WIDTH", screen.width);
            display.put("SCREEN_HEIGHT", screen.height);
            displays.put(display);

            obj.put("SCREENS", displays);

            instance.SCREENS = new Screen[1];
            instance.SCREENS[0] = screen;
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

        obj.put("CURSOR_IMAGE", instance.CURSOR_IMAGE);
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

            JSONArray screens = configJson.getJSONArray("SCREENS");
            instance.SCREENS = new Screen[screens.length()];
            for (int i = 0; i < instance.SCREENS.length; i++) {
                JSONObject screen = screens.getJSONObject(i);
                int dx = screen.getInt("SCREEN_X");
                int dy = screen.getInt("SCREEN_Y");
                int dw = screen.getInt("SCREEN_WIDTH");
                int dh = screen.getInt("SCREEN_HEIGHT");
                instance.SCREENS[i] = new Screen(dx, dy, dw, dh);
            }

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

            instance.CURSOR_IMAGE = configJson.getString("CURSOR_IMAGE");
        } catch (Exception e) {
            Config.print("Error: config.json is malformed or outdated!");
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

    public Screen[] SCREENS;
    public int SELECTED_SCREEN = 0;

    public Screen getScreen() {
        return SCREENS[SELECTED_SCREEN];
    }

    public int FPS = 30;

    public int ADD_FRAMES_LATENCY = 2;

    public String CURSOR_IMAGE = "";
    public float QUALITY_ALPHA = 1f;
    public float KEYFRAME_THRESHOLD = 0.03f;
    public float KEYFRAME_THRESHOLD_SUM = 0.16f;

    public String HIGH_FORMAT = Constants.JPEG;
    public String LOW_FORMAT = Constants.JPEG;
    public String INTERFRAME_FORMAT = Constants.JPEG;

    // 1 is no frameskip, 2 every other frame, 3 every 2 in 3 frames are skipped
    public int MAX_FRAME_SKIP = 3;

    public float MIN_QUALITY = .3f;
    public float MAX_QUALITY = .9f;
}
