package org.example.network.api.response;

public class InvalidMethodApiResponse extends TextApiResponse {

    public InvalidMethodApiResponse() {
        super(405, "Method not allowed");
    }
}
