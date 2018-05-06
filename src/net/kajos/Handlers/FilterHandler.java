package net.kajos.Handlers;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class FilterHandler implements HttpHandler {
    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        response.header("Access-Control-Allow-Origin", "*");
        control.nextHandler();
    }
}
