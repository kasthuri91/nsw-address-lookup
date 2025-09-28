package com.nsw.cs.Exception;

import java.io.IOException;

public class HttpServerException extends IOException {
    private final int statusCode;

    public HttpServerException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}