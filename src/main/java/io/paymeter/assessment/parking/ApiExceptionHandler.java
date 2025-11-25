package io.paymeter.assessment.parking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleParsing(DateTimeParseException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid date format");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(message, code, status.value()));
    }

    private static class ErrorResponse {
        private final String message;
        private final String code;
        private final int status;

        ErrorResponse(String message, String code, int status) {
            this.message = message;
            this.code = code;
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public String getCode() {
            return code;
        }

        public int getStatus() {
            return status;
        }
    }
}
