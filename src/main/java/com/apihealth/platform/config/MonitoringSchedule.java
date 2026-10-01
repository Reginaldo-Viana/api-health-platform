package com.apihealth.platform.config;

import com.apihealth.platform.service.DailyReportService;
import com.apihealth.platform.service.MonitoringService;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitoringSchedule {

    private final MonitoringService monitoringService;
    private final DailyReportService reportService;

    public MonitoringSchedule(MonitoringService monitoringService, DailyReportService reportService) {
        this.monitoringService = monitoringService;
        this.reportService = reportService;
    }

    @Scheduled(fixedDelayString = "${monitoring.interval-ms:300000}", initialDelay = 30000)
    public void monitorApis() {
        monitoringService.runAll();
    }

    @Scheduled(cron = "${monitoring.daily-report-cron:0 5 0 * * *}")
    public void generatePreviousDayReport() {
        reportService.generate(LocalDate.now().minusDays(1));
    }
}