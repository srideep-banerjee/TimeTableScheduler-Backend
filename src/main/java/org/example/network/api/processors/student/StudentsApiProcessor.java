package org.example.network.api.processors.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.StudentDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.Student;

import java.io.IOException;
import java.util.Iterator;

public class StudentsApiProcessor extends ApiProcessor {

    @Override
    public String getEndpoint() {
        return "/io/students";
    }

    @Override
    public boolean matches(ApiRequest request) {
        String path = request.path();
        return path.equals("/io/students");
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        switch (request.method()) {
            case "GET" -> {
                try {
                    String response = objectMapper.writeValueAsString(StudentDao.getInstance());
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
                    String roll = it.next();
                    if (roll.isEmpty()) {
                        return new TextApiResponse(400, "Student roll number can't be empty");
                    }
                    JsonNode studentJson = arr.get(roll);
                    try {
                        Student student = objectMapper.reader().readValue(studentJson, Student.class);
                        StudentDao.getInstance().put(roll, student);
                    } catch (IOException e) {
                        return new TextApiResponse(400, "Invalid data format");
                    }
                }
                return new TextApiResponse(200, "Students updated");
            }
            case "DELETE" -> {
                StudentDao.getInstance().clear();
                return new TextApiResponse(200, "Request accepted");
            }
            default -> {
                return new InvalidMethodApiResponse();
            }
        }
    }
}
