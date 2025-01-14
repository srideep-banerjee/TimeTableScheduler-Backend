package org.example.network.api.response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class TextApiResponse implements ApiResponse {
    private final int code;
    private final String response;

    public TextApiResponse(int code, String response) {
        this.code = code;
        this.response = response;
    }

    @Override
    public void send(HttpExchange exchange, String allowedOrigin) {
        try {
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "text/plain");
            headers.set("Access-Control-Allow-Origin", allowedOrigin);
            exchange.sendResponseHeaders(code, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
