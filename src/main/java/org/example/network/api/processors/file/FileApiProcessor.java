package org.example.network.api.processors.file;

import org.example.files.TTSFileException;
import org.example.network.api.ApiRequest;
import org.example.network.api.processors.ApiProcessor;
import org.example.network.api.response.ApiResponse;
import org.example.network.api.response.ServerErrorApiResponse;
import org.example.network.api.response.TextApiResponse;

import java.io.IOException;
import java.sql.SQLException;

public class FileApiProcessor extends ApiProcessor {
    private final String endpoint;
    private final FileApiInterface apiInterface;

    public FileApiProcessor(String endpoint, FileApiInterface apiInterface) {
        this.endpoint = endpoint;
        this.apiInterface = apiInterface;
    }

    @Override
    public boolean matches(ApiRequest request) {
        return request.path().equals("/io/saves/" + endpoint) &&
                request.queries().size() == 1 &&
                request.queries().containsKey("name");
    }

    @Override
    public ApiResponse process(ApiRequest request) {
        String name = request.queries().get("name");

        try {
            apiInterface.perform(name);
            return new TextApiResponse(200, "Request accepted");
        } catch (TTSFileException ex) {
            return new TextApiResponse(400, ex.getMessage());
        } catch (SQLException | IOException e) {
            System.err.println(e);
            e.printStackTrace();
            return new ServerErrorApiResponse();
        }
    }

    public interface FileApiInterface {
        void perform(String name) throws SQLException, IOException;
    }
}
