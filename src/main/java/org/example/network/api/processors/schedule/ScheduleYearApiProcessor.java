package org.example.network.api.processors.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.ScheduleSolution;

public class ScheduleYearApiProcessor extends ApiProcessor {

    @Override
    public String getEndpoint() {
        return "/io/schedule";
    }

    public ScheduleYearApiProcessor() {
        super.priority = 2;
    }

    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals(getEndpoint()) &&
                request.queries().size() == 1 &&
                request.queries().containsKey("year");
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        if (!request.method().equals("GET")) {
            return new InvalidMethodApiResponse();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        int year;
        try {
            year = Integer.parseInt(request.queries().get("year"));
        } catch (NumberFormatException e) {
            return new TextApiResponse(400, "Year must be a number");
        }
        String response;
        try {
            response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getData(year));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ServerErrorApiResponse();
        }
        if (response.equals("null")) return new TextApiResponse(400, "Year or section invalid");
        else return new JsonApiResponse(200, response);
    }
}
