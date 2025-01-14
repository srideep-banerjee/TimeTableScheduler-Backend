package org.example.network.api.processors.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.ScheduleStructure;

import java.io.IOException;

public class ScheduleStructureApiProcessor extends ApiProcessor {
    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals("/io/schedule/structure");
    }

    @Override
    public ApiResponse process(ApiRequest request, HttpExchange exchange) {
        ObjectMapper objectMapper = new ObjectMapper();
        switch (request.method()) {
            case "GET" -> {
                try {
                    String response = objectMapper.writeValueAsString(ScheduleStructure.getInstance());
                    return new JsonApiResponse(200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return new ServerErrorApiResponse();
                }
            }
            case "PUT" -> {
                try {
                    objectMapper.readerForUpdating(ScheduleStructure.getInstance()).readValue(exchange.getRequestBody());
                    ScheduleSolution.getInstance().updateStructure();
                    return new TextApiResponse(200, "Request accepted");
                } catch (IOException e) {
                    e.printStackTrace();
                    return new TextApiResponse(400, "Invalid data format");
                }
            }
            default -> {
                return new InvalidMethodApiResponse();
            }
        }
    }
}
