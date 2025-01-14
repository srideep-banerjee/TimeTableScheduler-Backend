package org.example.network.api.response;

public class InvalidTokenApiResponse extends TextApiResponse {

    public InvalidTokenApiResponse() {
        super(400, "Invalid token");
    }
}
