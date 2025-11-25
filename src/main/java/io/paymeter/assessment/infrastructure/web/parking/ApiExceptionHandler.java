package io.paymeter.assessment.infrastructure.web.parking;

import io.paymeter.assessment.application.shared.BadRequestException;
import io.paymeter.assessment.application.shared.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return buildResponse(ex.getMessage(), "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return buildResponse(ex.getMessage(), "NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TicketController.TicketBadRequestException.class)
    public ResponseEntity<ErrorResponse> handleParsingError(TicketController.TicketBadRequestException ex) {
        return buildResponse(ex.getMessage(), "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return buildResponse(ex.getMessage(), "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, String code, HttpStatus status) {
        ErrorResponse body = new ErrorResponse(message, code, status.value(), Instant.now().toString());
        return ResponseEntity.status(status).body(body);
    }

    public static class ErrorResponse {
        private final String message;
        private final String code;
        private final int status;
        private final String timestamp;

        public ErrorResponse(String message, String code, int status, String timestamp) {
            this.message = message;
            this.code = code;
            this.status = status;
            this.timestamp = timestamp;
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

        public String getTimestamp() {
            return timestamp;
        }
    }
}
