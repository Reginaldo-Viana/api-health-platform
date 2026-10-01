package com.apihealth.platform.controller;

import com.apihealth.platform.model.DailyReport;
import com.apihealth.platform.service.DailyReportService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class DailyReportController {

    private final DailyReportService reportService;

    public DailyReportController(DailyReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/latest")
    public DailyReport latest() {
        return reportService.latest();
    }

    @GetMapping("/{date}")
    public DailyReport find(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.find(date);
    }

    @PostMapping("/{date}/generate")
    public DailyReport generate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.generate(date);
    }
}