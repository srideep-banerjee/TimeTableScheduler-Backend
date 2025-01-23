package org.example.network.api.processors.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.files.SavesHandler;
import org.example.files.TTSFileException;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.JsonApiResponse;
import org.example.network.api.response.ServerErrorApiResponse;
import org.example.network.api.response.TextApiResponse;

import java.io.IOException;
import java.sql.SQLException;

public class FileListApiProcessor extends ApiProcessor {

    @Override
    public String getEndpoint() {
        return "/io/saves/list";
    }

    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals(getEndpoint());
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String response = objectMapper.writeValueAsString(SavesHandler.getInstance().getSavesList());
            return new JsonApiResponse(200, response);
        } catch (TTSFileException ex) {
            return new TextApiResponse(400, ex.getMessage());
        } catch (SQLException | IOException e) {
            System.err.println(e);
            e.printStackTrace();
            return new ServerErrorApiResponse();
        }
    }
}
