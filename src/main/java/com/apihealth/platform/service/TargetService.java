package com.apihealth.platform.service;

import com.apihealth.platform.dto.TargetRequest;
import com.apihealth.platform.model.ApiTarget;
import com.apihealth.platform.repository.ApiTargetRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TargetService {

    private final ApiTargetRepository targetRepository;

    public TargetService(ApiTargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    public List<ApiTarget> findAll() {
        return targetRepository.findAllByOrderByNameAsc();
    }

    public ApiTarget create(TargetRequest request) {
        if (targetRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um alvo com esse nome");
        }
        return targetRepository.save(new ApiTarget(request.getName(), request.getUrl(),
                request.getMethod().toUpperCase()));
    }

    public ApiTarget update(Long id, TargetRequest request) {
        ApiTarget target = targetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alvo não encontrado"));
        targetRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um alvo com esse nome");
                });
        target.setName(request.getName());
        target.setUrl(request.getUrl());
        target.setMethod(request.getMethod().toUpperCase());
        return targetRepository.save(target);
    }

    public void disable(Long id) {
        ApiTarget target = targetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alvo não encontrado"));
        target.setEnabled(false);
        targetRepository.save(target);
    }
}