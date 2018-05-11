package net.kajos.Manager;

import net.kajos.Server;
import org.json.JSONArray;
import org.json.JSONObject;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

import java.awt.*;
import java.util.*;

public class Manager extends BaseWebSocketHandler {
    private HashMap<WebSocketConnection, Viewer> viewers = new HashMap<>();
    private Input input;
    private Server server;

    public Manager(Server server) throws AWTException {
        input = new Input();
        this.server = server;
    }

    public void sendImage(Viewer viewer, byte[] data) {
        Iterator<Map.Entry<WebSocketConnection, Viewer>> it = viewers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<WebSocketConnection, Viewer> entry = it.next();
            if (entry.getValue().equals(viewer)) {
                WebSocketConnection conn = entry.getKey();
                conn.send(data);
                return;
            }
        }
    }

    public void sendEmptyImage(Viewer viewer, int framestamp) {
        byte[] data = new byte[]{0, 0, 0, 0, 0};
        data[1] = (byte) (framestamp & 0xff);
        framestamp >>= 8;
        data[2] = (byte) (framestamp & 0xff);
        framestamp >>= 8;
        data[3] = (byte) (framestamp & 0xff);
        framestamp >>= 8;
        data[4] = (byte) (framestamp & 0xff);
        sendImage(viewer, data);
    }

    private void closeConnection(WebSocketConnection conn) {
        if (conn != null) {
            conn.close();
            if (viewers.containsKey(conn)) viewers.remove(conn);
        }
    }

    public void onOpen(WebSocketConnection conn) {
        viewers.put(conn, new Viewer());
        System.out.println("Connection opened");
    }

    public void onClose(WebSocketConnection conn) {
        closeConnection(conn);

        System.out.println("Connection closed");
    }

    public Collection<Viewer> getViewers() {
        return viewers.values();
    }

    @Override
    public void onMessage(WebSocketConnection connection, String message) {
        Viewer viewer = viewers.get(connection);

        if (viewer == null) return;

        if (message.startsWith(">")) {
            viewer.frameUpdate(Integer.parseInt(message.substring(1)));
        } else {
            JSONObject obj = new JSONObject(message);
            if (obj.has("window")) {
                JSONArray ar = obj.getJSONArray("window");
                viewer.clientWidth = ar.getInt(0);
                viewer.clientHeight = ar.getInt(1);
                viewer.reset();
                server.resize(viewer.clientWidth, viewer.clientHeight);
            }
            input.parseInput(obj);
        }
    }
}
