package com.apihealth.platform.repository;

import com.apihealth.platform.model.DailyReport;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
    Optional<DailyReport> findByReportDate(LocalDate reportDate);
    Optional<DailyReport> findTopByOrderByReportDateDesc();
}