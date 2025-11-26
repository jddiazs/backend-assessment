package io.paymeter.assessment.infrastructure.web.parking;

import io.paymeter.assessment.application.shared.BadRequestException;
import io.paymeter.assessment.application.shared.NotFoundException;
import io.paymeter.assessment.infrastructure.web.parking.dto.ErrorResponse;
import io.paymeter.assessment.infrastructure.web.parking.exception.TicketBadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return buildResponse("Authentication required", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse("Access denied", "FORBIDDEN", HttpStatus.FORBIDDEN);
    }

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

    @ExceptionHandler(TicketBadRequestException.class)
    public ResponseEntity<ErrorResponse> handleParsingError(TicketBadRequestException ex) {
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
}
