package io.paymeter.assessment.infrastructure.web.parking.exception;

public class TicketBadRequestException extends RuntimeException {

    public TicketBadRequestException(String message) {
        super(message);
    }
}
