package net.kajos;

import java.awt.*;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws InterruptedException, AWTException, URISyntaxException {
        Server server = new Server();
        server.start();
    }
}
