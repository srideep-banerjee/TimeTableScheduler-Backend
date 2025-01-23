package org.example.network.api.processors.teacher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public String getEndpoint() {
        return "/io/teachers/names";
    }

    @Override
    public boolean matches(ApiRequest request) {
        String endpoint = getEndpoint();
        return request.path().equals(endpoint);
    }

    @Override
    public ApiResponse process(ApiRequest request) {
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
