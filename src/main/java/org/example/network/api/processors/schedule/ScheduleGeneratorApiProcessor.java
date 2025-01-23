package org.example.network.api.processors.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.algorithms.Generator;
import org.example.files.SavesHandler;
import org.example.interfaces.OnResultListener;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.*;
import org.example.pojo.ScheduleSolution;

public class ScheduleGeneratorApiProcessor extends ApiProcessor {

    public ScheduleGeneratorApiProcessor() {
        super.priority = 2;
    }

    @Override
    public String getEndpoint() {
        return "/io/schedule";
    }

    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals(getEndpoint()) &&
                request.queries().size() == 1 &&
                request.queries()
                        .getOrDefault("generatenew", "false")
                        .equals("true");
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        if (!request.method().equals("GET")) {
            return new InvalidMethodApiResponse();
        }

        final ObjectMapper objectMapper = new ObjectMapper();
        final ApiResponse[] apiResponse = {null};
        final Object lock = new Object();

        Generator generator = new Generator(new OnResultListener() {
            @Override
            public void onResult() {
                try {
                    String response = objectMapper.writeValueAsString(ScheduleSolution.getInstance().getData());
                    apiResponse[0] =  new JsonApiResponse(200, response);
                    SavesHandler.getInstance().markUnsaved();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    apiResponse[0] = new ServerErrorApiResponse();
                }
                synchronized (lock) {
                    lock.notify();
                }
                System.gc();
            }

            @Override
            public void onError(String msg) {
                apiResponse[0] = new TextApiResponse(500, msg);
                lock.notify();
                System.gc();
            }
        });
        generator.generate();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return apiResponse[0];
    }
}
