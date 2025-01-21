package org.example.network.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.DefaultConfig;
import org.example.algorithms.Generator;
import org.example.files.SavesHandler;
import org.example.network.TokenManager;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.processors.ApiProcessorList;
import org.example.network.api.response.*;

import java.io.IOException;
import java.util.*;

public class ApiHandler implements HttpHandler {
    HttpServer server;
    ObjectMapper objectMapper;
    Generator generator;
    List<ApiProcessor> apiProcessors;

    public ApiHandler(HttpServer server) {
        this.server = server;
        objectMapper = new ObjectMapper();
        generator = new Generator(null);
        apiProcessors = ApiProcessorList.getAvailableApiProcessors();
    }

    public ApiProcessor getApiProcessor(ApiRequest request) {
        return apiProcessors
                .stream()
                .filter(processor -> processor.matches(request))
                .max(Comparator.comparingInt(p -> p.priority)).orElse(null);
    }

    @Override
    public void handle(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();

        String allowedOrigin = "http://localhost:" + server.getAddress().getPort();
        if (!DefaultConfig.REQUIRE_TOKEN)
            allowedOrigin = "*";

        List<String> apiTokenHeader = exchange.getRequestHeaders().get("Api-Token");
        if (DefaultConfig.REQUIRE_TOKEN && (apiTokenHeader == null || apiTokenHeader.isEmpty() || !apiTokenHeader.get(0).equals(TokenManager.token))) {
            new InvalidTokenApiResponse().send(exchange, allowedOrigin);
            return;
        }
        String requestMethod = exchange.getRequestMethod();
        String querys = exchange.getRequestURI().getQuery();
        System.out.println(requestMethod + " " + path + (querys != null ? "?" + querys : ""));
        if (requestMethod.equals("OPTIONS")) {
            new PreflightApiResponse().send(exchange, allowedOrigin);
            return;
        }

        ApiRequest apiRequest = null;
        try {
            apiRequest = ApiRequest.fromHttpExchange(exchange);
        } catch (IOException e) {
            new ServerErrorApiResponse().send(exchange, allowedOrigin);
        }
        ApiProcessor apiProcessor = getApiProcessor(apiRequest);
        ApiResponse apiResponse;
        try {
            if (apiProcessor == null) {
                apiResponse = new TextApiResponse(405, "Unsupported request");
            } else {
                apiResponse = apiProcessor.process(apiRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            apiResponse = new ServerErrorApiResponse();
        }
        apiResponse.send(exchange, allowedOrigin);

        if (
                !isResponseUnsuccessful(apiResponse) &&
                        !requestMethod.equals("GET") &&
                        !path.startsWith("/io/saves") &&
                        !path.startsWith("/io/config/global/")
        ) {
            SavesHandler.getInstance().markUnsaved();
        }
    }

    private boolean isResponseUnsuccessful(ApiResponse response) {
        if (!(response instanceof TextApiResponse textApiResponse)) return false;
        return textApiResponse.code / 100 != 2;
    }
}
