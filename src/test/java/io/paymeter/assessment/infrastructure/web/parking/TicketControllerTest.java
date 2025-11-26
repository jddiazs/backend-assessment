package io.paymeter.assessment.infrastructure.web.parking;

import io.paymeter.assessment.application.pricing.PricingService;
import io.paymeter.assessment.application.pricing.dto.CalculationResult;
import io.paymeter.assessment.domain.pricing.Money;
import io.paymeter.assessment.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = TicketController.class)
@Import({ApiExceptionHandler.class, SecurityConfig.class, TicketControllerTest.FixedClockConfig.class})
class TicketControllerTest {

    @Autowired
    private WebTestClient webTestClient;

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
    @WithMockUser(username = "user", roles = "USER")
    void shouldCalculateWithDefaultToWhenNotProvided() {
        String from = "2024-02-27T09:00:00";
        ZonedDateTime fromDate = ZonedDateTime.of(LocalDateTime.parse(from), ZoneOffset.UTC);
        ZonedDateTime toDate = ZonedDateTime.ofInstant(Instant.parse("2024-02-27T10:00:00Z"), ZoneOffset.UTC);

        CalculationResult result = new CalculationResult(
                "P000123",
                fromDate,
                toDate,
                60,
                new Money(200)
        );
        when(pricingService.calculate(eq("P000123"), eq(fromDate), isNull())).thenReturn(Mono.just(result));

        String body = """
                {
                  "parkingId": "P000123",
                  "from": "2024-02-27T09:00:00"
                }
                """;

        webTestClient.post()
                .uri("/tickets/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.parkingId").isEqualTo("P000123")
                .jsonPath("$.from").value(org.hamcrest.Matchers.startsWith("2024-02-27T09:00"))
                .jsonPath("$.to").value(org.hamcrest.Matchers.startsWith("2024-02-27T10:00"))
                .jsonPath("$.duration").isEqualTo(60)
                .jsonPath("$.price").isEqualTo("200EUR");
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldReturnBadRequestForInvalidDate() {
        String body = """
                {
                  "parkingId": "P000123",
                  "from": "invalid"
                }
                """;

        webTestClient.post()
                .uri("/tickets/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("BAD_REQUEST");
    }

    @Test
    void shouldReturnUnauthorizedWithoutCredentials() {
        String body = """
                {
                  "parkingId": "P000123",
                  "from": "2024-02-27T09:00:00"
                }
                """;

        webTestClient.post()
                .uri("/tickets/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
