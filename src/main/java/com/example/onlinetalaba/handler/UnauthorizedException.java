package com.example.onlinetalaba.handler;

public class UnauthorizedException extends ErrorMessageException {

    public UnauthorizedException(String message) {
        super(message, ErrorCodes.Unauthorized);
    }

    public UnauthorizedException(String message, String userMessage) {
        super(message, ErrorCodes.Unauthorized, userMessage);
    }
}
