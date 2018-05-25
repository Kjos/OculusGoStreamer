package net.kajos.Manager;

import net.kajos.AudioRecorder;
import net.kajos.Config;
import net.kajos.Server;
import org.json.JSONArray;
import org.json.JSONObject;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

import java.awt.*;

public class AudioManager extends BaseWebSocketHandler {
    private WebSocketConnection connection;

    private AudioRecorder audioRecorder;

    public AudioManager() throws AWTException {
        audioRecorder = new AudioRecorder(this);
    }

    public void sendData(byte[] data, int len) {
        if (connection == null) return;

        connection.send(data, 0, len);
    }

    private void closeConnection(WebSocketConnection conn) {
        if (conn != null) {
            conn.close();
        }
        connection = null;
    }

    public void onOpen(WebSocketConnection conn) {
        connection = conn;
        System.out.println("Connection opened");
    }

    public void onClose(WebSocketConnection conn) {
        closeConnection(conn);

        System.out.println("Connection closed");
    }

    @Override
    public void onMessage(WebSocketConnection connection, String message) {
    }
}
