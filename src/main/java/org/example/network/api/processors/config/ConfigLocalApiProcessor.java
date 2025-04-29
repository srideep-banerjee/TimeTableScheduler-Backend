package org.example.network.api.processors.config;

import org.example.dao.ConfigDao;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.InvalidMethodApiResponse;
import org.example.network.api.response.TextApiResponse;

public class ConfigLocalApiProcessor extends ApiProcessor {

    @Override
    public String getEndpoint() {
        return "/io/config/local/";
    }

    @Override
    public boolean matches(ApiRequest request) {
        String path = request.path();
        return request.path().startsWith(getEndpoint()) && path.length() > 18;
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        String path = request.path();
        String keyName = path.substring(18);

        switch (request.method()) {
            case "GET" -> {
                String value = ConfigDao.getInstance().get(keyName);
                if (value == null) {
                    return new TextApiResponse(404, "null");
                } else {
                    return new TextApiResponse(200, value);
                }
            }
            case "PUT" -> {
                String value = request.body();
                ConfigDao.getInstance().put(keyName, value);
                return new TextApiResponse(200, "Request accepted");
            }
            case "DELETE" -> {
                ConfigDao.getInstance().remove(keyName);
                return new TextApiResponse(200, "Request accepted");
            }
            default -> {
                return new InvalidMethodApiResponse();
            }
        }
    }
}
