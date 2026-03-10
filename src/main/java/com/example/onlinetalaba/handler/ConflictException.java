package com.example.onlinetalaba.handler;

public class ConflictException extends ErrorMessageException {

    public ConflictException(String message) {
        super(message, ErrorCodes.AlreadyExists);
    }

    public ConflictException(String message, String userMessage) {
        super(message, ErrorCodes.AlreadyExists, userMessage);
    }
}
