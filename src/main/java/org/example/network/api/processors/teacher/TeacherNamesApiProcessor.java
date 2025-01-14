package org.example.network.api.processors.teacher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.dao.TeacherDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.InvalidMethodApiResponse;
import org.example.network.api.response.JsonApiResponse;
import org.example.network.api.response.ServerErrorApiResponse;

public class TeacherNamesApiProcessor extends ApiProcessor {

    public TeacherNamesApiProcessor() {
        super.priority = 2;
    }

    @Override
    public boolean matches(ApiRequest request) {
        String endpoint = "/io/teachers/names";
        return request.path().equals(endpoint);
    }

    @Override
    public ApiResponse process(ApiRequest request, HttpExchange exchange) {
        if (request.method().equals("GET")) {

            try {
                String response = new ObjectMapper().writeValueAsString(TeacherDao.getInstance().keySet());
                return new JsonApiResponse(200, response);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return new ServerErrorApiResponse();
            }
        }
        return new InvalidMethodApiResponse();
    }
}
