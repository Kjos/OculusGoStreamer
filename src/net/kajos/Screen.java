package net.kajos;

import java.awt.*;
import java.util.ArrayList;

public class Screen {
    public int x, y, width, height;

    public Screen(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static ArrayList<Screen> getScreens() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        ArrayList<Screen> screenList = new ArrayList<>();
        for(GraphicsDevice curGs : gs)
        {
            GraphicsConfiguration[] gc = curGs.getConfigurations();
            for(GraphicsConfiguration curGc : gc)
            {
                Rectangle bounds = curGc.getBounds();
                screenList.add(new Screen(bounds.x, bounds.y, bounds.width, bounds.height));
                break;
            }
        }
        return screenList;
    }
}
