package org.example.network.api.response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class JsonApiResponse implements ApiResponse {
    private final int code;
    private final String jsonString;

    public JsonApiResponse(int code, String jsonString) {
        this.code = code;
        this.jsonString = jsonString;
    }

    @Override
    public void send(HttpExchange exchange, String allowedOrigin) {
        try {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Access-Control-Allow-Origin", allowedOrigin);
            exchange.sendResponseHeaders(code, jsonString.length());
            OutputStream os = exchange.getResponseBody();
            os.write(jsonString.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
