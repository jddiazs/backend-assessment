package io.paymeter.assessment.infrastructure.web.parking;

import io.paymeter.assessment.application.pricing.PricingService;
import io.paymeter.assessment.application.shared.NotFoundException;
import io.paymeter.assessment.infrastructure.web.parking.dto.TicketRequest;
import io.paymeter.assessment.infrastructure.web.parking.dto.TicketResponse;
import io.paymeter.assessment.infrastructure.web.parking.exception.TicketBadRequestException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

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
            return ZonedDateTime.of(LocalDateTime.parse(value), ZoneOffset.UTC);
        }
    }
}
