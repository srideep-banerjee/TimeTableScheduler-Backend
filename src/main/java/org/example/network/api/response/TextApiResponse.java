package org.example.network.api.response;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TextApiResponse implements ApiResponse {
    public final int code;
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
            byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, responseData.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseData);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
