package com.example.onlinetalaba.handler;

public class BadRequestException extends ErrorMessageException {

    public BadRequestException(String message) {
        super(message, ErrorCodes.BadRequest);
    }

    public BadRequestException(String message, String userMessage) {
        super(message, ErrorCodes.BadRequest, userMessage);
    }
}
