package io.paymeter.assessment.parking;

import io.paymeter.assessment.pricing.Money;
import io.paymeter.assessment.pricing.PricingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketController.class)
@Import({ApiExceptionHandler.class, TicketControllerTest.FixedClockConfig.class})
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PricingService pricingService;

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Test
    void shouldCalculateWithDefaultToWhenNotProvided() throws Exception {
        String from = "2024-02-27T09:00:00";
        ZonedDateTime fromDate = ZonedDateTime.of(LocalDateTime.parse(from), ZoneOffset.UTC);
        ZonedDateTime toDate = ZonedDateTime.ofInstant(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC);

        PricingService.CalculationResult result = new PricingService.CalculationResult(
                "P000123",
                fromDate,
                toDate,
                60,
                new Money(200)
        );
        when(pricingService.calculate(eq("P000123"), eq(fromDate), eq(toDate))).thenReturn(result);

        String body = """
                {
                  "parkingId": "P000123",
                  "from": "2024-02-27T09:00:00"
                }
                """;

        mockMvc.perform(post("/tickets/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingId").value("P000123"))
                .andExpect(jsonPath("$.from", startsWith("2024-02-27T09:00:00")))
                .andExpect(jsonPath("$.to", startsWith("2024-02-27T10:00:00")))
                .andExpect(jsonPath("$.duration").value(60))
                .andExpect(jsonPath("$.price").value("200EUR"));
    }

    @Test
    void shouldReturnBadRequestForInvalidDate() throws Exception {
        String body = """
                {
                  "parkingId": "P000123",
                  "from": "invalid"
                }
                """;

        mockMvc.perform(post("/tickets/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
