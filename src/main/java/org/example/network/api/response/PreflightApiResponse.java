package org.example.network.api.response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.example.DefaultConfig;

import java.io.IOException;
import java.io.OutputStream;

public class PreflightApiResponse implements ApiResponse {
    @Override
    public void send(HttpExchange exchange, String allowedOrigin) {
        try {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Access-Control-Allow-Origin", allowedOrigin);
            headers.set("Access-Control-Allow-Methods","GET,PUT,POST,DELETE");
            headers.set("Access-Control-Allow-Headers","Content-Type, api-token");
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
