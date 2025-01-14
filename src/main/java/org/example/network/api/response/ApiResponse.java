package org.example.network.api.response;

import com.sun.net.httpserver.HttpExchange;

public interface ApiResponse {
    void send(HttpExchange exchange, String allowedOrigin);
}
