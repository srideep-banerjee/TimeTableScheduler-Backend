package org.example.network.api.processors.config;

import org.example.files.SavesHandler;
import org.example.files.db.ConfigHandler;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.InvalidMethodApiResponse;
import org.example.network.api.response.ServerErrorApiResponse;
import org.example.network.api.response.TextApiResponse;

import java.sql.SQLException;

public class ConfigGlobalApiProcessor extends ApiProcessor {
    @Override
    public boolean matches(ApiRequest request) {
        String path = request.path();
        return request.path().startsWith("/io/config/global/") && path.length() > 18;
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        String path = request.path();
        String keyName = path.substring(18);
        ConfigHandler configHandler = SavesHandler.getInstance().getConfigHandler();

        try {
            switch (request.method()) {
                case "GET" -> {
                    String value = configHandler.getGlobal(keyName);
                    if (value == null) {
                        return new TextApiResponse(404, "null");
                    } else {
                        return new TextApiResponse(200, value);
                    }
                }
                case "PUT" -> {
                    String value = request.body();
                    configHandler.putGlobal(keyName, value);
                    return new TextApiResponse(200, "Request accepted");
                }
                case "DELETE" -> {
                    configHandler.deleteGlobal(keyName);
                    return new TextApiResponse(200, "Request accepted");
                }
                default -> {
                    return new InvalidMethodApiResponse();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ServerErrorApiResponse();
        }
    }
}
