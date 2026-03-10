package com.example.onlinetalaba.handler;

public class NotFoundException extends ErrorMessageException {

    public NotFoundException(String message) {
        super(message, ErrorCodes.NotFound);
    }

    public NotFoundException(String message, String userMessage) {
        super(message, ErrorCodes.NotFound, userMessage);
    }
}
