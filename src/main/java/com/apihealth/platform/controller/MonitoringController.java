package com.apihealth.platform.controller;

import com.apihealth.platform.model.CheckResult;
import com.apihealth.platform.service.MonitoringService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @PostMapping("/run")
    public List<CheckResult> runAll() {
        return monitoringService.runAll();
    }

    @PostMapping("/run/{targetId}")
    public CheckResult runOne(@PathVariable Long targetId) {
        return monitoringService.runOne(targetId);
    }

    @GetMapping("/checks")
    public List<CheckResult> recent(@RequestParam(required = false) Long targetId) {
        return monitoringService.recent(targetId);
    }
}