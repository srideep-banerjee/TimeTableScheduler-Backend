package org.example.network.api.processors.subject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.SubjectDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.Subject;

import java.io.IOException;
import java.util.Iterator;

public class SubjectsApiProcessor extends ApiProcessor {

    @Override
    public String getEndpoint() {
        return "/io/subjects";
    }

    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals(getEndpoint());
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        switch (request.method()) {
            case "GET" -> {
                try {
                    String response = objectMapper.writeValueAsString(SubjectDao.getInstance());
                    return new JsonApiResponse(200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return new ServerErrorApiResponse();
                }
            }
            case "PUT" -> {
                JsonNode arr;
                try {
                    arr = objectMapper.readTree(request.body());
                } catch (IOException e) {
                    return new TextApiResponse(400, "Invalid data format");
                }
                for (Iterator<String> it = arr.fieldNames(); it.hasNext(); ) {
                    String code = it.next();
                    if (code.isEmpty()) {
                        return new TextApiResponse(400, "Subject code can't be empty");
                    } else if (code.length() > 20) {
                        return new TextApiResponse(400, "Subject code can't be longer than 20 characters");
                    }
                    JsonNode subJson = arr.get(code);
                    try {
                        Subject subject = objectMapper.reader().readValue(subJson, Subject.class);
                        SubjectDao.getInstance().put(code, subject);
                    } catch (IOException e) {
                        return new TextApiResponse(400, "Invalid data format");
                    }
                }
                return new TextApiResponse(200, "Subjects updated");
            }
            case "DELETE" -> {
                ScheduleSolution.getInstance().resetData();
                return new TextApiResponse(200, "Request accepted");
            }
            default -> {
                return new InvalidMethodApiResponse();
            }
        }
    }
}
