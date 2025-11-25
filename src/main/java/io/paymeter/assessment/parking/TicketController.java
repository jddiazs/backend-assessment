package io.paymeter.assessment.parking;

import io.paymeter.assessment.pricing.Money;
import io.paymeter.assessment.pricing.PricingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@RestController
@RequestMapping("tickets")
public class TicketController {

    private static final DateTimeFormatter RESPONSE_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final PricingService pricingService;
    private final Clock clock;

    public TicketController(PricingService pricingService, Clock clock) {
        this.pricingService = pricingService;
        this.clock = clock;
    }

    @PostMapping("/calculate")
    public CalculationResponse calculate(@RequestBody CalculationRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        ZonedDateTime from = parseDateTime(request.getFrom(), "from");
        ZonedDateTime to = request.getTo() == null ? ZonedDateTime.now(clock) : parseDateTime(request.getTo(), "to");

        PricingService.CalculationResult result = pricingService.calculate(request.getParkingId(), from, to);

        return CalculationResponse.from(result);
    }

    private ZonedDateTime parseDateTime(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " is required");
        }
        try {
            return ZonedDateTime.parse(value);
        } catch (DateTimeParseException first) {
            return parseWithoutZone(value);
        }
    }

    private ZonedDateTime parseWithoutZone(String value) {
        LocalDateTime localDateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
    }

    public static class CalculationRequest {
        private String parkingId;
        private String from;
        private String to;

        public String getParkingId() {
            return parkingId;
        }

        public void setParkingId(String parkingId) {
            this.parkingId = parkingId;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }

    public static class CalculationResponse {
        private String parkingId;
        private String from;
        private String to;
        private long duration;
        private String price;

        public static CalculationResponse from(PricingService.CalculationResult result) {
            Objects.requireNonNull(result, "CalculationResult is required");
            CalculationResponse response = new CalculationResponse();
            response.parkingId = result.getParkingId();
            response.from = RESPONSE_FORMATTER.format(result.getFrom());
            response.to = RESPONSE_FORMATTER.format(result.getTo());
            response.duration = result.getDurationMinutes();
            response.price = formatPrice(result.getPrice());
            return response;
        }

        private static String formatPrice(Money price) {
            return price == null ? null : price.format();
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
}
