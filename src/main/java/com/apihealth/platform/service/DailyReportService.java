package com.apihealth.platform.service;

import com.apihealth.platform.model.CheckResult;
import com.apihealth.platform.model.DailyReport;
import com.apihealth.platform.repository.CheckResultRepository;
import com.apihealth.platform.repository.DailyReportRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DailyReportService {

    private final CheckResultRepository checkRepository;
    private final DailyReportRepository reportRepository;

    public DailyReportService(CheckResultRepository checkRepository, DailyReportRepository reportRepository) {
        this.checkRepository = checkRepository;
        this.reportRepository = reportRepository;
    }

    public DailyReport generate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<CheckResult> checks = checkRepository.findByCheckedAtGreaterThanEqualAndCheckedAtLessThan(start, end);
        long successful = checks.stream().filter(CheckResult::isAvailable).count();
        long total = checks.size();
        double availability = total == 0 ? 0 : successful * 100.0 / total;
        double averageLatency = total == 0 ? 0 : checks.stream()
                .mapToLong(CheckResult::getLatencyMs).average().orElse(0);

        DailyReport report = reportRepository.findByReportDate(date)
            .orElseGet(() -> new DailyReport(date, total, successful, total - successful,
                availability, averageLatency, LocalDateTime.now()));
        report.updateTotals(total, successful, total - successful, availability,
            averageLatency, LocalDateTime.now());
        return reportRepository.save(report);
    }

    public DailyReport find(LocalDate date) {
        return reportRepository.findByReportDate(date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado"));
    }

    public DailyReport latest() {
        return reportRepository.findTopByOrderByReportDateDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum relatório disponível"));
    }
}