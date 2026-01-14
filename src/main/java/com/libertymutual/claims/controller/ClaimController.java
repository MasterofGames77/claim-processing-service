package com.libertymutual.claims.controller;

import com.libertymutual.claims.dto.ClaimRequest;
import com.libertymutual.claims.dto.ClaimResponse;
import com.libertymutual.claims.model.ClaimSummary;
import com.libertymutual.claims.service.ClaimService;
import com.libertymutual.claims.service.PerformanceMonitoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Slf4j
public class ClaimController {
    
    private final ClaimService claimService;
    private final PerformanceMonitoringService performanceMonitoringService;
    
    @PostMapping
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody ClaimRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Received POST /api/claims request");
            ClaimResponse response = claimService.createClaim(request);
            
            long duration = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordRequestTime("POST /api/claims", duration);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordRequestTime("POST /api/claims", duration);
            log.error("Error creating claim", e);
            throw e;
        }
    }
    
    @GetMapping("/summary")
    public ResponseEntity<ClaimSummary> getSummary() {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Received GET /api/claims/summary request");
            ClaimSummary summary = claimService.getSummary();
            
            long duration = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordRequestTime("GET /api/claims/summary", duration);
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            performanceMonitoringService.recordRequestTime("GET /api/claims/summary", duration);
            log.error("Error fetching summary", e);
            throw e;
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", LocalDateTime.now()));
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<PerformanceMonitoringService.PerformanceMetrics> getMetrics() {
        return ResponseEntity.ok(performanceMonitoringService.getMetrics());
    }
    
    // Inner classes for response
    public record HealthResponse(String status, LocalDateTime timestamp) {}
}
