package net.kajos.Handlers;
import net.kajos.Manager.Manager;
import net.kajos.Manager.Viewer;
import net.kajos.Server;
import net.kajos.Util;
import org.json.JSONObject;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.Iterator;
import java.util.Set;

public class ControlsHandler implements HttpHandler {
    private Manager manager;
    private Server server;
    
    public ControlsHandler(Server server, Manager manager) {
        this.server = server;
        this.manager = manager;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        Viewer player = manager.ensureViewer();

        Set<String> qParamKeys = request.queryParamKeys();
        Iterator<String> keyIt = qParamKeys.iterator();

        while (keyIt.hasNext()) {
            String key = keyIt.next();
            String value = request.queryParam(key);

            try {
                int val = Integer.valueOf(value);
                switch(key) {
                    case "WIDTH":
                        player.clientWidth = val;
                        break;
                    case "HEIGHT":
                        player.clientHeight = val;
                        break;
                }

                player.newUpdate();
            } catch (NumberFormatException e) {
                System.out.println("Malformed integer passing by");
                continue;
            }
        }

        server.resize(player.clientWidth, player.clientHeight);

        JSONObject obj = new JSONObject();

        response.header("Content-type", "text/json")
                .header("Access-Control-Allow-Origin", "*")
                .content(obj.toString(1))
                .end();
    }
}
