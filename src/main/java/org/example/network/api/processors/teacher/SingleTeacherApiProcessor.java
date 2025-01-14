package org.example.network.api.processors.teacher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.dao.TeacherDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.Teacher;

import java.io.IOException;
import java.util.regex.Pattern;

public class SingleTeacherApiProcessor extends ApiProcessor {
    @Override
    public boolean matches(ApiRequest request) {
        String path = request.path();
        return path.startsWith("/io/teachers/") && (path.length() > "/io/teachers/".length());
    }

    @Override
    public ApiResponse process(ApiRequest request, HttpExchange exchange) {
        ObjectMapper objectMapper = new ObjectMapper();
        String path = request.path();
        String name = path.substring(path.lastIndexOf("/") + 1).toUpperCase();
        switch (request.method()) {
            case "GET" -> {
                if (!TeacherDao.getInstance().containsKey(name)) {
                    return new TextApiResponse(404, "Teacher not found");
                }
                try {
                    String response = objectMapper.writeValueAsString(TeacherDao.getInstance().get(name));
                    return new JsonApiResponse(200, response);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return new ServerErrorApiResponse();
                }
            }
            case "PUT" -> {
                if (Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]").matcher(name).find()) {
                    return new TextApiResponse(400, "name must not contain special character");
                } else if (name.isEmpty()) {
                    return new TextApiResponse(400, "name can't be empty");
                } else if (name.length() > 50) {
                    return new TextApiResponse(400, "name can't be longer than 50 characters");
                }
                try {
                    TeacherDao.getInstance().put(name, objectMapper.readValue(exchange.getRequestBody(), Teacher.class));
                    return new TextApiResponse(200, "Request accepted");
                } catch (IOException e) {
                    return new TextApiResponse(400, "Invalid data format");
                }
            }
            case "DELETE" -> {
                if (!TeacherDao.getInstance().containsKey(name)) {
                    return new TextApiResponse(404, "Teacher not found");
                }
                ScheduleSolution.getInstance().removeTeacherByName(name);
                TeacherDao.getInstance().remove(name);
                return new TextApiResponse(200, "Request accepted");
            }
            default -> {
                return new InvalidMethodApiResponse();
            }
        }
    }
}
