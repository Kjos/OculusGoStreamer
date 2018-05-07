package net.kajos.Manager;

import net.kajos.Config;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class Input {
    private Robot robot;

    public Input() throws AWTException {
        robot = new Robot();
    }

    private void keyPress(int key) {
        robot.keyPress(key);
    }

    private void keyRelease(int key) {
        robot.keyRelease(key);
    }

    private void mousePress() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
    }

    private void mouseRelease() {
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    private void mouseMove(int x, int y) {
        int nx = Config.get().SCREEN_LEFT + x * Config.get().SCREEN_WIDTH / 10000;
        int ny = Config.get().SCREEN_TOP + y * Config.get().SCREEN_HEIGHT / 10000;

        robot.mouseMove(nx, ny);
    }

    private long tPress = 0;
    public void parseInput(String json) {
        JSONObject obj = new JSONObject(json);
        if (obj.has("mouseMove")) {
            JSONArray pos = obj.getJSONArray("mouseMove");
            mouseMove(pos.getInt(0), pos.getInt(1));
        }

        if (obj.has("mousePress")) {
            JSONArray pos = obj.getJSONArray("mousePress");
            mouseMove(pos.getInt(0), pos.getInt(1));

            mousePress();
            tPress = System.currentTimeMillis();
        }

        if (obj.has("mouseRelease")) {
            JSONArray pos = obj.getJSONArray("mouseRelease");
            mouseMove(pos.getInt(0), pos.getInt(1));

            long d = 30L - (System.currentTimeMillis() - tPress);
            if (d > 0) try {
                Thread.sleep(d);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mouseRelease();
        }

        if (obj.has("keys")) {
            String keys = obj.getString("keys");

            StringSelection stringSelection = new StringSelection(keys);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);

            keyPress(KeyEvent.VK_CONTROL);
            keyPress(KeyEvent.VK_V);
            keyRelease(KeyEvent.VK_V);
            keyRelease(KeyEvent.VK_CONTROL);
        }
    }
}
