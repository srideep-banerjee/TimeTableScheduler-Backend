package org.example.network.api.processors.file;

import com.sun.net.httpserver.HttpExchange;
import org.example.files.SavesHandler;
import org.example.files.TTSFileException;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.ServerErrorApiResponse;
import org.example.network.api.response.TextApiResponse;

import java.io.IOException;
import java.sql.SQLException;

public class FileCurrentNameApiProcessor extends ApiProcessor {
    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals("/io/saves/currentName");
    }

    @Override
    public ApiResponse process(ApiRequest request, HttpExchange exchange) {
        try {
            String saveName = SavesHandler.getInstance().getCurrentSaveName();
            return new TextApiResponse(200, saveName);
        } catch (TTSFileException ex) {
            return new TextApiResponse(400, ex.getMessage());
        } catch (SQLException | IOException e) {
            System.err.println(e);
            e.printStackTrace();
            return new ServerErrorApiResponse();
        }
    }
}
