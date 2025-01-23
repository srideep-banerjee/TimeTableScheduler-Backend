package org.example.network.api.processors.subject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.SubjectDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.InvalidMethodApiResponse;
import org.example.network.api.response.JsonApiResponse;
import org.example.network.api.response.ServerErrorApiResponse;

public class SubjectCodesApiProcessor extends ApiProcessor {

    public SubjectCodesApiProcessor() {
        super.priority = 2;
    }

    @Override
    public String getEndpoint() {
        return "/io/subjects/codes";
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
        String response;
        try {
            response = new ObjectMapper().writeValueAsString(SubjectDao.getInstance().keySet());
            return new JsonApiResponse(200, response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ServerErrorApiResponse();
        }
    }
}
