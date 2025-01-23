package org.example.network.api.processors.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.InvalidMethodApiResponse;
import org.example.network.api.response.JsonApiResponse;
import org.example.network.api.response.ServerErrorApiResponse;
import org.example.pojo.ScheduleSolution;

public class ScheduleApiProcessor extends ApiProcessor {

    @Override
    public String getEndpoint() {
        return "/io/schedule";
    }

    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals(getEndpoint());
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        if (!request.method().equals("GET")) {
            return new InvalidMethodApiResponse();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getData());
            return new JsonApiResponse(200, response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ServerErrorApiResponse();
        }
    }
}
