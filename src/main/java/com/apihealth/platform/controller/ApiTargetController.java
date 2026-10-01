package com.apihealth.platform.controller;

import com.apihealth.platform.dto.TargetRequest;
import com.apihealth.platform.model.ApiTarget;
import com.apihealth.platform.service.SeedData;
import com.apihealth.platform.service.TargetService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/targets")
public class ApiTargetController {

    private final TargetService targetService;
    private final SeedData seedData;

    public ApiTargetController(TargetService targetService, SeedData seedData) {
        this.targetService = targetService;
        this.seedData = seedData;
    }

    @GetMapping
    public List<ApiTarget> list() {
        return targetService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiTarget create(@Valid @RequestBody TargetRequest request) {
        return targetService.create(request);
    }

    @PutMapping("/{id}")
    public ApiTarget update(@PathVariable Long id, @Valid @RequestBody TargetRequest request) {
        return targetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable Long id) {
        targetService.disable(id);
    }

    @PostMapping("/seed")
    public List<ApiTarget> refreshSeed() {
        seedData.seedMissingTargets();
        return targetService.findAll();
    }
}