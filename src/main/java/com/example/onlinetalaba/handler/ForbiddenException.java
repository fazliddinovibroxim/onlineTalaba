package com.example.onlinetalaba.handler;

public class ForbiddenException extends ErrorMessageException {

    public ForbiddenException(String message) {
        super(message, ErrorCodes.Forbidden);
    }

    public ForbiddenException(String message, String userMessage) {
        super(message, ErrorCodes.Forbidden, userMessage);
    }
}
