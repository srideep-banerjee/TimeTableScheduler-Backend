package org.example.network.api.processors.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.TeacherDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.InvalidMethodApiResponse;
import org.example.network.api.response.ServerErrorApiResponse;
import org.example.network.api.response.TextApiResponse;
import org.example.pojo.ScheduleSolution;

public class ScheduleTeacherApiProcessor extends ApiProcessor {
    @Override
    public boolean matches(ApiRequest request) {
        String path = request.path();
        return path.startsWith("/io/schedule/teacher/") && path.length() > 21;
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        if (!request.method().equals("GET")) {
            return new InvalidMethodApiResponse();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String path = request.path();
        String name = path.substring(path.lastIndexOf("/") + 1).toUpperCase();

        if (!TeacherDao.getInstance().containsKey(name)) {
            return new TextApiResponse(404, "Teacher not found");
        }
        if (ScheduleSolution.getInstance().isEmpty()) {
            return new TextApiResponse(404, "Schedule is empty");
        }
        try {
            String response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getTeacherScheduleByName(name));
            return new TextApiResponse(200, response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ServerErrorApiResponse();
        }
    }
}
