package org.example.network.api.processors.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.ScheduleSolution;

import java.util.List;

public class ScheduleSectionYearApiProcessor extends ApiProcessor {

    public ScheduleSectionYearApiProcessor() {
        super.priority = 3;
    }

    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals("/io/schedule") &&
                request.queries().size() == 2 &&
                request.queries().containsKey("year") &&
                request.queries().containsKey("sec");
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        int year;
        try {
            year = Integer.parseInt(request.queries().get("year"));
        } catch (NumberFormatException e) {
            return new TextApiResponse(400, "Year must be a number");
        }
        int sec;
        try {
            sec = Integer.parseInt(request.queries().get("sec"));
        } catch (NumberFormatException e) {
            return new TextApiResponse(400, "Section must be a number");
        }
        switch (request.method()) {
            case "GET" -> {
                String response;
                try {
                    response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getData(year, sec));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return new ServerErrorApiResponse();
                }
                if (response.equals("null")) return new TextApiResponse(400, "Semester or section invalid");
                else return new JsonApiResponse(200, response);
            }
            case "PUT" -> {
                try {
                    List<List<List<String>>> data = objectMapper.readValue(request.body(), new TypeReference<>() {
                    });

                    String error = ScheduleSolution.getInstance().setData(year, sec, data);
                    if (error != null) {
                        return new TextApiResponse(400, error);
                    }
                    return new TextApiResponse(200, "Request accepted");
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ServerErrorApiResponse();
                }
            }
            default -> {
                return new InvalidMethodApiResponse();
            }
        }
    }
}
