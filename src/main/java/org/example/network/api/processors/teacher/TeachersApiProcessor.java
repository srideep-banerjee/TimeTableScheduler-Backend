package org.example.network.api.processors.teacher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.TeacherDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.Teacher;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

public class TeachersApiProcessor extends ApiProcessor {

    @Override
    public boolean matches(ApiRequest request) {
        String endpoint = "/io/teachers";
        return request.path().equals(endpoint);
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        switch (request.method()) {
            case "GET" -> {
                try {
                    String response = objectMapper.writeValueAsString(TeacherDao.getInstance());
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
                    String name = it.next();
                    if (Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]").matcher(name).find()) {
                        return new TextApiResponse(400, "Name must not contain special character");
                    } else if (name.isEmpty()) {
                        return new TextApiResponse(400, "Name can't be empty");
                    } else if (name.length() > 50) {
                        return new TextApiResponse(400, "Name can't be longer than 50 characters");
                    }
                    JsonNode teacherJson = arr.get(name);
                    try {
                        Teacher teacher = objectMapper.reader().readValue(teacherJson, Teacher.class);
                        TeacherDao.getInstance().put(name, teacher);
                    } catch (IOException e) {
                        return new TextApiResponse(400, "Invalid data format");
                    }
                }
                return new TextApiResponse(200, "Teachers updated");
            }
            case "DELETE" -> {
                ScheduleSolution.getInstance().removeAllTeachers();
                TeacherDao.getInstance().clear();
                return new TextApiResponse(200, "Request accepted");
            }
            default -> {
                return new InvalidMethodApiResponse();
            }
        }
    }
}
