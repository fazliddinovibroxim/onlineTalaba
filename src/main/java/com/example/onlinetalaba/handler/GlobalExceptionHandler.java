package com.example.onlinetalaba.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<ApiErrorResponse> handleLoginException(LoginException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "LOGIN_ERROR", e.getMessage(), request, null);
    }

    @ExceptionHandler(ErrorMessageException.class)
    public ResponseEntity<ApiErrorResponse> handleErrorMessageException(
            ErrorMessageException e,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(e.getErrorCode().getStatusCode());
        String message = e.getUserMessage() != null ? e.getUserMessage() : e.getMessage();
        return buildResponse(status, e.getErrorCode().getName(), message, request, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException e,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorCodes.Unauthorized.getName(), e.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException e,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, ErrorCodes.Forbidden.getName(), "Access denied", request, null);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            Exception e,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();

        if (e instanceof MethodArgumentNotValidException manve) {
            for (FieldError fieldError : manve.getBindingResult().getFieldErrors()) {
                validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else if (e instanceof HandlerMethodValidationException hmve) {
            validationErrors.put("request", hmve.getReason());
        } else if (e instanceof ConstraintViolationException cve) {
            cve.getConstraintViolations().forEach(violation ->
                    validationErrors.put(violation.getPropertyPath().toString(), violation.getMessage())
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.InvalidParams.getName(),
                "Validation failed",
                request,
                validationErrors
        );
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            HttpMessageNotReadableException.class,
            MultipartException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequestExceptions(
            Exception e,
            HttpServletRequest request
    ) {
        String message = e instanceof MultipartException
                ? "Invalid multipart request"
                : (e.getMessage() == null ? "Bad request" : e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BadRequest.getName(), message, request, null);
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            MaxUploadSizeExceededException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflictAndUploadExceptions(
            Exception e,
            HttpServletRequest request
    ) {
        String message = e instanceof MaxUploadSizeExceededException
                ? "Uploaded file is too large"
                : "Database integrity violation";
        HttpStatus status = e instanceof MaxUploadSizeExceededException ? HttpStatus.BAD_REQUEST : HttpStatus.CONFLICT;
        String code = e instanceof MaxUploadSizeExceededException
                ? ErrorCodes.BadRequest.getName()
                : ErrorCodes.AlreadyExists.getName();
        return buildResponse(status, code, message, request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException e,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ErrorCodes.NotFound.getName(), "Resource not found", request, null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException e,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCodes.BadRequest.getName(), e.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.InternalServerError.getName(),
                "Internal server error",
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors
    ) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors == null || validationErrors.isEmpty() ? null : validationErrors)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
