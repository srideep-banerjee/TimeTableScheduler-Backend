package org.example.network.api.response;

import com.sun.net.httpserver.HttpExchange;

public class ServerErrorApiResponse extends TextApiResponse {

    public ServerErrorApiResponse() {
        super(500, "Internal Server Error");
    }
}
