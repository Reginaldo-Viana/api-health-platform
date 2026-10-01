package com.apihealth.platform.repository;

import com.apihealth.platform.model.ApiTarget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiTargetRepository extends JpaRepository<ApiTarget, Long> {
    List<ApiTarget> findAllByOrderByNameAsc();
    List<ApiTarget> findByEnabledTrueOrderByNameAsc();
    Optional<ApiTarget> findByNameIgnoreCase(String name);
}