package com.apihealth.platform.service;

import com.apihealth.platform.model.ApiTarget;
import com.apihealth.platform.model.CheckResult;
import com.apihealth.platform.repository.ApiTargetRepository;
import com.apihealth.platform.repository.CheckResultRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class MonitoringService {

    private final ApiTargetRepository targetRepository;
    private final CheckResultRepository checkRepository;
    private final RestTemplate restTemplate;

    public MonitoringService(ApiTargetRepository targetRepository, CheckResultRepository checkRepository,
                             RestTemplate restTemplate) {
        this.targetRepository = targetRepository;
        this.checkRepository = checkRepository;
        this.restTemplate = restTemplate;
    }

    public List<CheckResult> runAll() {
        return targetRepository.findByEnabledTrueOrderByNameAsc().stream()
                .map(this::check)
                .collect(java.util.stream.Collectors.toList());
    }

    public CheckResult runOne(Long targetId) {
        ApiTarget target = targetRepository.findById(targetId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Alvo não encontrado"));
        return check(target);
    }

    public List<CheckResult> recent(Long targetId) {
        return targetId == null
                ? checkRepository.findTop100ByOrderByCheckedAtDesc()
                : checkRepository.findTop100ByTargetIdOrderByCheckedAtDesc(targetId);
    }

    private CheckResult check(ApiTarget target) {
        long startedAt = System.nanoTime();
        Integer statusCode = null;
        boolean available = false;
        String errorMessage = null;

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    target.getUrl(), HttpMethod.valueOf(target.getMethod()), null, Void.class);
            statusCode = response.getStatusCodeValue();
            available = statusCode < 500;
            if (!available) {
                errorMessage = "Resposta HTTP " + statusCode;
            }
        } catch (HttpStatusCodeException exception) {
            statusCode = exception.getRawStatusCode();
            available = statusCode < 500;
            errorMessage = available ? null : "Resposta HTTP " + statusCode;
        } catch (RestClientException exception) {
            errorMessage = exception.getMessage();
        }

        long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        if (errorMessage != null && errorMessage.length() > 500) {
            errorMessage = errorMessage.substring(0, 500);
        }
        return checkRepository.save(new CheckResult(target.getId(), target.getName(), target.getUrl(),
                LocalDateTime.now(), latencyMs, statusCode, available, errorMessage));
    }
}