package net.kajos.Manager;

import net.kajos.AudioRecorder;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

public class AudioManager extends BaseWebSocketHandler {
    private WebSocketConnection connection;
    private AudioRecorder audio;

    public AudioManager() {
        audio = new AudioRecorder(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Audio Mbit: " + (bytes / 1024 / 1024));
                    bytes = 0;
                }
            }
        }).start();
    }

    public void onOpen(WebSocketConnection conn) {
        connection = conn;
        System.out.println("Audio connection opened");
    }

    public void onClose(WebSocketConnection conn) {
        connection = null;
        conn.close();

        System.out.println("Audio connection closed");
    }

    int bytes = 0;
    public void sendData(byte[] data, int len) {
        if (connection == null) return;

        connection.send(data, 0, len);
        bytes += len;
    }

    @Override
    public void onMessage(WebSocketConnection connection, String message) {
    }
}
