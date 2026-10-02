package com.apihealth.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apihealth.platform.model.ApiTarget;
import com.apihealth.platform.model.DailyReport;
import com.apihealth.platform.repository.ApiTargetRepository;
import com.apihealth.platform.repository.DailyReportRepository;
import com.apihealth.platform.service.MonitoringService;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:api-health-integration;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "monitoring.interval-ms=3600000",
                "monitoring.daily-report-cron=-"
        })
class ApiHttpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApiTargetRepository targetRepository;

    @Autowired
    private DailyReportRepository reportRepository;

    @MockitoBean
    private MonitoringService monitoringService;

    @Test
        @SuppressWarnings("null")
    void managesTargetsThroughHttpAndPersistsThemInH2() {
        String name = "integration-" + UUID.randomUUID();
        Map<String, String> createRequest = Map.of(
                "name", name,
                "url", "https://example.com/health",
                "method", "GET");

        ResponseEntity<JsonNode> created = restTemplate.postForEntity(
                "/api/targets", createRequest, JsonNode.class);

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        JsonNode createdBody = Objects.requireNonNull(created.getBody());
        long targetId = createdBody.path("id").asLong();
        assertTrue(targetId > 0);

        ApiTarget savedTarget = targetRepository.findById(targetId).orElseThrow();
        assertEquals(name, savedTarget.getName());
        assertEquals("GET", savedTarget.getMethod());
        assertTrue(savedTarget.isEnabled());

        Map<String, String> updateRequest = Map.of(
                "name", name + "-updated",
                "url", "https://example.com/status",
                "method", "HEAD");
        ResponseEntity<JsonNode> updated = restTemplate.exchange(
                "/api/targets/" + targetId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                JsonNode.class);

        assertEquals(HttpStatus.OK, updated.getStatusCode());
        ApiTarget updatedTarget = targetRepository.findById(targetId).orElseThrow();
        assertEquals(name + "-updated", updatedTarget.getName());
        assertEquals("https://example.com/status", updatedTarget.getUrl());
        assertEquals("HEAD", updatedTarget.getMethod());

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/targets/" + targetId, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());
        assertFalse(targetRepository.findById(targetId).orElseThrow().isEnabled());
    }

    @Test
    void rejectsInvalidTargetThroughHttp() {
        String name = "invalid-integration-" + UUID.randomUUID();
        Map<String, String> request = Map.of(
                "name", name,
                "url", "ftp://example.com/health",
                "method", "POST");

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/api/targets", request, JsonNode.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(targetRepository.findByNameIgnoreCase(name).isEmpty());
    }

    @Test
    void generatesAndReadsReportThroughHttpWithH2Persistence() {
        LocalDate date = LocalDate.of(2097, 4, 5);

        ResponseEntity<JsonNode> generated = restTemplate.postForEntity(
                "/api/reports/" + date + "/generate", HttpEntity.EMPTY, JsonNode.class);

        assertEquals(HttpStatus.OK, generated.getStatusCode());
        JsonNode generatedBody = Objects.requireNonNull(generated.getBody());
        assertEquals(0, generatedBody.path("totalChecks").asLong());
        assertEquals(0, generatedBody.path("failedChecks").asLong());

        DailyReport savedReport = reportRepository.findByReportDate(date).orElseThrow();
        assertEquals(0, savedReport.getTotalChecks());
        assertEquals(0, savedReport.getAverageLatencyMs());

        ResponseEntity<JsonNode> byDate = restTemplate.getForEntity(
                "/api/reports/" + date, JsonNode.class);
        ResponseEntity<JsonNode> latest = restTemplate.getForEntity(
                "/api/reports/latest", JsonNode.class);

        assertEquals(HttpStatus.OK, byDate.getStatusCode());
        assertEquals(date.toString(), Objects.requireNonNull(byDate.getBody()).path("reportDate").asText());
        assertEquals(HttpStatus.OK, latest.getStatusCode());
        assertEquals(date.toString(), Objects.requireNonNull(latest.getBody()).path("reportDate").asText());
    }

    @Test
    void returnsNotFoundForReportThatDoesNotExist() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/reports/2098-12-31", JsonNode.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}