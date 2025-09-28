package com.nsw.cs.Exception;

import java.io.IOException;

public class HttpClientException extends IOException {
    private final int statusCode;

    public HttpClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
