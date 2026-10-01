package com.apihealth.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void generatesSummaryFromChecksForDate() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        when(checkRepository.findByCheckedAtGreaterThanEqualAndCheckedAtLessThan(
                date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(Arrays.asList(
                        check(120, true),
                        check(80, false)));
        when(reportRepository.findByReportDate(date)).thenReturn(Optional.empty());
        when(reportRepository.save(any(DailyReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyReport result = service.generate(date);

        assertEquals(2, result.getTotalChecks());
        assertEquals(1, result.getSuccessfulChecks());
        assertEquals(1, result.getFailedChecks());
        assertEquals(50.0, result.getAvailabilityPercentage());
        assertEquals(100.0, result.getAverageLatencyMs());
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

        assertEquals(0, result.getTotalChecks());
        assertEquals(0.0, result.getAvailabilityPercentage());
        verify(reportRepository).save(existing);
    }

    private CheckResult check(long latencyMs, boolean available) {
        return new CheckResult(1L, "Example", "https://example.com", LocalDateTime.now(),
                latencyMs, available ? 200 : 503, available, null);
    }
}