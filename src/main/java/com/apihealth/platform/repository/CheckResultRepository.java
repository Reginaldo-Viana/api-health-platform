package com.apihealth.platform.repository;

import com.apihealth.platform.model.CheckResult;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findTop100ByOrderByCheckedAtDesc();
    List<CheckResult> findTop100ByTargetIdOrderByCheckedAtDesc(Long targetId);
    List<CheckResult> findByCheckedAtGreaterThanEqualAndCheckedAtLessThan(
            LocalDateTime start, LocalDateTime end);
}