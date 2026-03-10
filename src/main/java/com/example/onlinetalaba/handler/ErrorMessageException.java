package com.example.onlinetalaba.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorMessageException extends RuntimeException {
    private final String message;
    private final ErrorCodes errorCode;
    private final String userMessage;

    public ErrorMessageException(String message, ErrorCodes errorCode) {
        this.message = message;
        this.errorCode = errorCode;
        this.userMessage = null;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
