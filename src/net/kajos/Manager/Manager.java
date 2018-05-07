package net.kajos.Manager;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

import java.awt.*;

public class Manager extends BaseWebSocketHandler {
    private Viewer viewer = null;
    private Input input;
    private WebSocketConnection connection = null;

    public Manager() throws AWTException {
        input = new Input();
    }

    public void sendImage(byte[] data) {
        if (connection == null) return;

        connection.send(data);
    }

    public void sendEmptyImage(int framestamp) {
        byte[] data = new byte[]{0, 0, 0, 0, 0};
        data[1] = (byte) (framestamp & 0xff);
        framestamp >>= 8;
        data[2] = (byte) (framestamp & 0xff);
        framestamp >>= 8;
        data[3] = (byte) (framestamp & 0xff);
        framestamp >>= 8;
        data[4] = (byte) (framestamp & 0xff);
        sendImage(data);
    }

    private void closeConnection() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void onOpen(WebSocketConnection conn) {
        closeConnection();

        connection = conn;

        System.out.println("Connection opened");
    }

    public void onClose(WebSocketConnection conn) {
        connection = null;
        viewer = null;

        System.out.println("Connection closed");
    }

    public Viewer getViewer() {
        return viewer;
    }

    public Viewer createNewViewer() {
        viewer = new Viewer();
        return viewer;
    }

    @Override
    public void onMessage(WebSocketConnection connection, String message) {
        if (viewer == null) return;

        if (message.startsWith(">")) {
            viewer.frameUpdate(Integer.parseInt(message.substring(1)));
        } else {
            input.parseInput(message);
        }
    }
}
