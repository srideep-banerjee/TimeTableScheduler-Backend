package org.example.network.api.processors.file;

import org.example.files.SavesHandler;
import org.example.files.TTSFileException;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.TextApiResponse;

public class FileIsSavedApiProcessor extends ApiProcessor {
    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals("/io/saves/isSaved");
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        try {
            return new TextApiResponse(200, SavesHandler.getInstance().isSaved() + "");
        } catch (TTSFileException ex) {
            return new TextApiResponse(400, ex.getMessage());
        }
    }
}
