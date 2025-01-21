package org.example.network.api.processors.subject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.SubjectDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.Subject;

import java.io.IOException;

public class SingleSubjectApiProcessor extends ApiProcessor {
    @Override
    public boolean matches(ApiRequest request) {
        String path = request.path();
        return path.startsWith("/io/subjects/") && (path.length() > "/io/subjects/".length());
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        String path = request.path();
        String code = path.substring(path.lastIndexOf("/") + 1).toUpperCase();
        switch (request.method()) {
            case "GET" -> {
                if (!SubjectDao.getInstance().containsKey(code)) {
                    return new TextApiResponse(404, "Subject not found");
                }
                try {
                    String response = objectMapper.writeValueAsString(SubjectDao.getInstance().get(code));
                    return new JsonApiResponse(200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return new ServerErrorApiResponse();
                }
            }
            case "PUT" -> {
                if (code.isEmpty()) {
                    return new TextApiResponse(400, "Subject code can't be empty");
                } else if (code.length() > 20) {
                    return new TextApiResponse(400, "Subject code can't be longer than 20 characters");
                }
                try {
                    SubjectDao.getInstance().put(code, objectMapper.readValue(request.body(), Subject.class));
                    return new TextApiResponse(200, "Request accepted");
                } catch (IOException e) {
                    return new TextApiResponse(400, "Invalid data format");
                }
            }
            case "DELETE" -> {
                if (!SubjectDao.getInstance().containsKey(code)) {
                    new TextApiResponse(404, "Subject not found");
                }
                ScheduleSolution.getInstance().removeSubjectByCode(code);
                SubjectDao.getInstance().remove(code);
                return new TextApiResponse(200, "Request accepted");
            }
            default -> {
                return new InvalidMethodApiResponse();
            }

        }
    }
}
