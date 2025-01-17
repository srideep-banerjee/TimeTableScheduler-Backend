package org.example.network.api.processors.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.dao.StudentDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.Student;

import java.io.IOException;

public class SingleStudentApiProcessor extends ApiProcessor {
    @Override
    public boolean matches(ApiRequest request) {
        String path = request.path();
        return path.startsWith("/io/students/") && (path.length() > "/io/students/".length());
    }

    @Override
    public ApiResponse process(ApiRequest request, HttpExchange exchange) {
        ObjectMapper objectMapper = new ObjectMapper();
        String path = request.path();
        String roll = path.substring(path.lastIndexOf("/") + 1).toUpperCase();
        switch (request.method()) {
            case "GET" -> {
                if (!StudentDao.getInstance().containsKey(roll)) {
                    return new TextApiResponse(404, "Student not found");
                }
                try {
                    String response = objectMapper.writeValueAsString(StudentDao.getInstance().get(roll));
                    return new JsonApiResponse(200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return new ServerErrorApiResponse();
                }
            }
            case "PUT" -> {
                if (roll.isEmpty()) {
                    return new TextApiResponse(400, "Student roll number can't be empty");
                }
                try {
                    StudentDao.getInstance().put(roll, objectMapper.readValue(exchange.getRequestBody(), Student.class));
                    return new TextApiResponse(200, "Request accepted");
                } catch (IOException e) {
                    return new TextApiResponse(400, "Invalid data format");
                }
            }
            case "DELETE" -> {
                if (!StudentDao.getInstance().containsKey(roll)) {
                    new TextApiResponse(404, "Student not found");
                }
                StudentDao.getInstance().remove(roll);
                return new TextApiResponse(200, "Request accepted");
            }
            default -> {
                return new InvalidMethodApiResponse();
            }

        }
    }
}
