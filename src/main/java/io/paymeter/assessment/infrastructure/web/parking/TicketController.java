package io.paymeter.assessment.infrastructure.web.parking;

import io.paymeter.assessment.application.pricing.PricingService;
import io.paymeter.assessment.application.shared.NotFoundException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final PricingService pricingService;

    public TicketController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/calculate")
    @Tag(name = "calculate", description = "calculate the price per parking space")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = TicketBadRequestException.class))),
            @ApiResponse(responseCode = "404", description = "Parking not found", content = @Content(schema = @Schema(implementation = NotFoundException.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = RuntimeException.class)))
    })
    public Mono<TicketResponse> calculate(@Valid @RequestBody TicketRequest request) {
        try {
            ZonedDateTime from = parseDate(request.getFrom());
            ZonedDateTime to = request.getTo() != null ? parseDate(request.getTo()) : null;

            return pricingService.calculate(request.getParkingId(), from, to)
                    .map(result -> new TicketResponse(
                            result.getParkingId(),
                            result.getFrom().toString(),
                            result.getTo().toString(),
                            result.getDurationMinutes(),
                            result.getPrice().format()
                    ));
        } catch (DateTimeParseException e) {
            throw new TicketBadRequestException("Invalid date format");
        }
    }

    private ZonedDateTime parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ZonedDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            // Fallback to UTC when input omits offset
            return ZonedDateTime.of(LocalDateTime.parse(value), ZoneOffset.UTC);
        }
    }

    public static class TicketRequest {
        @NotBlank
        private String parkingId;
        @NotBlank
        private String from;
        private String to;

        public String getParkingId() {
            return parkingId;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }

    public static class TicketResponse {
        private final String parkingId;
        private final String from;
        private final String to;
        private final long duration;
        private final String price;

        public TicketResponse(String parkingId, String from, String to, long duration, String price) {
            this.parkingId = parkingId;
            this.from = from;
            this.to = to;
            this.duration = duration;
            this.price = price;
        }

        public String getParkingId() {
            return parkingId;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public long getDuration() {
            return duration;
        }

        public String getPrice() {
            return price;
        }
    }

    static class TicketBadRequestException extends RuntimeException {
        TicketBadRequestException(String message) {
            super(message);
        }
    }
}
