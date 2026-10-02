package com.apihealth.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apihealth.platform.model.CheckResult;
import com.apihealth.platform.model.DailyReport;
import com.apihealth.platform.repository.CheckResultRepository;
import com.apihealth.platform.repository.DailyReportRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

    @Mock
    private CheckResultRepository checkRepository;

    @Mock
    private DailyReportRepository reportRepository;

    private DailyReportService service;

    @BeforeEach
    void setUp() {
        service = new DailyReportService(checkRepository, reportRepository);
    }

    @Test
    @SuppressWarnings("null")
    void generatesSummaryFromChecksForDate() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        when(checkRepository.findByCheckedAtGreaterThanEqualAndCheckedAtLessThan(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(Arrays.asList(
                        check(120, true),
                        check(80, false)));
        when(reportRepository.findByReportDate(date)).thenReturn(Optional.<DailyReport>empty());
        when(reportRepository.save(isA(DailyReport.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        DailyReport result = service.generate(date);

        assertEquals(2, result.getTotalChecks());
        assertEquals(1, result.getSuccessfulChecks());
        assertEquals(1, result.getFailedChecks());
        assertEquals(50.0, result.getAvailabilityPercentage());
        assertEquals(100.0, result.getAverageLatencyMs());
        assertEquals(date, result.getReportDate());
        verify(reportRepository).save(result);
    }

    @Test
    @SuppressWarnings("null")
    void createsZeroSummaryWhenThereAreNoChecksOrExistingReport() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        when(checkRepository.findByCheckedAtGreaterThanEqualAndCheckedAtLessThan(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(Collections.<CheckResult>emptyList());
        when(reportRepository.findByReportDate(date)).thenReturn(Optional.<DailyReport>empty());
        when(reportRepository.save(isA(DailyReport.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        DailyReport result = service.generate(date);

        assertEquals(date, result.getReportDate());
        assertEquals(0, result.getTotalChecks());
        assertEquals(0, result.getSuccessfulChecks());
        assertEquals(0, result.getFailedChecks());
        assertEquals(0.0, result.getAvailabilityPercentage());
        assertEquals(0.0, result.getAverageLatencyMs());
        verify(reportRepository).save(result);
    }

    @Test
    void updatesExistingReportInsteadOfCreatingDuplicateDate() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        DailyReport existing = new DailyReport(date, 1, 1, 0, 100, 50, LocalDateTime.now());
        when(checkRepository.findByCheckedAtGreaterThanEqualAndCheckedAtLessThan(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay())).thenReturn(Collections.emptyList());
        when(reportRepository.findByReportDate(date)).thenReturn(Optional.of(existing));
        when(reportRepository.save(existing)).thenReturn(existing);

        DailyReport result = service.generate(date);

        assertSame(existing, result);
        assertEquals(date, result.getReportDate());
        assertEquals(0, result.getTotalChecks());
        assertEquals(0, result.getSuccessfulChecks());
        assertEquals(0, result.getFailedChecks());
        assertEquals(0.0, result.getAvailabilityPercentage());
        assertEquals(0.0, result.getAverageLatencyMs());
        verify(reportRepository).save(existing);
    }

    @Test
    void findsReportByDate() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        DailyReport report = new DailyReport(date, 2, 1, 1, 50, 100, LocalDateTime.now());
        when(reportRepository.findByReportDate(date)).thenReturn(Optional.of(report));

        DailyReport result = service.find(date);

        assertSame(report, result);
        verify(reportRepository).findByReportDate(date);
    }

    @Test
    void throwsNotFoundWhenReportDateDoesNotExist() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        when(reportRepository.findByReportDate(date)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.find(date));

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());
    }

    @Test
    void findsLatestReport() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        DailyReport report = new DailyReport(date, 2, 1, 1, 50, 100, LocalDateTime.now());
        when(reportRepository.findTopByOrderByReportDateDesc()).thenReturn(Optional.of(report));

        DailyReport result = service.latest();

        assertSame(report, result);
        verify(reportRepository).findTopByOrderByReportDateDesc();
    }

    @Test
    void throwsNotFoundWhenNoLatestReportExists() {
        when(reportRepository.findTopByOrderByReportDateDesc()).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.latest());

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());
    }

    private CheckResult check(long latencyMs, boolean available) {
        return new CheckResult(1L, "Example", "https://example.com", LocalDateTime.now(),
                latencyMs, available ? 200 : 503, available, null);
    }
}