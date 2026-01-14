package com.libertymutual.claims.service;

import com.libertymutual.claims.dto.ClaimRequest;
import com.libertymutual.claims.dto.ClaimResponse;
import com.libertymutual.claims.model.Claim;
import com.libertymutual.claims.model.ClaimSummary;
import com.libertymutual.claims.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {
    
    private final ClaimRepository claimRepository;
    private final ClaimProcessingService claimProcessingService;
    private final CacheMetricsService cacheMetricsService;
    
    @Transactional
    public ClaimResponse createClaim(ClaimRequest request) {
        log.info("Creating new claim: type={}, amount={}, status={}", 
                request.getType(), request.getAmount(), request.getStatus());
        
        Claim claim = Claim.builder()
                .type(request.getType())
                .amount(request.getAmount())
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : java.time.LocalDateTime.now())
                .status(request.getStatus())
                .build();
        
        // JPA save() never returns null for new entities
        @SuppressWarnings("null")
        Claim savedClaim = claimRepository.save(claim);
        
        // Invalidate cache when new claim is added
        evictSummaryCache();
        
        // Process claim asynchronously
        Long claimId = savedClaim.getId();
        if (claimId != null) {
            claimProcessingService.processClaimAsync(claimId);
        } else {
            log.error("Claim saved but ID is null, cannot process asynchronously");
        }
        
        return mapToResponse(savedClaim);
    }
    
    @Cacheable(value = "claimSummary", key = "'summary'")
    @Transactional(readOnly = true)
    public ClaimSummary getSummary() {
        log.info("Fetching claim summary from database (cache miss)");
        cacheMetricsService.recordCacheMiss();
        
        List<Claim> allClaims = claimRepository.findAll();
        Long totalClaims = (long) allClaims.size();
        
        // Count by status
        Map<String, Long> claimsByStatus = allClaims.stream()
                .collect(Collectors.groupingBy(
                        Claim::getStatus,
                        Collectors.counting()
                ));
        
        // Sum amounts by status
        Map<String, BigDecimal> amountByStatus = allClaims.stream()
                .collect(Collectors.groupingBy(
                        Claim::getStatus,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Claim::getAmount,
                                BigDecimal::add
                        )
                ));
        
        // Total amount
        BigDecimal totalAmount = allClaims.stream()
                .map(Claim::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ClaimSummary.builder()
                .totalClaims(totalClaims)
                .claimsByStatus(claimsByStatus)
                .totalAmount(totalAmount)
                .amountByStatus(amountByStatus)
                .cacheHitCount(cacheMetricsService.getCacheHitCount().get())
                .cacheMissCount(cacheMetricsService.getCacheMissCount().get())
                .build();
    }
    
    @CacheEvict(value = "claimSummary", key = "'summary'")
    public void evictSummaryCache() {
        log.info("Evicting claim summary cache");
    }
    
    private ClaimResponse mapToResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .type(claim.getType())
                .amount(claim.getAmount())
                .timestamp(claim.getTimestamp())
                .status(claim.getStatus())
                .processedAt(claim.getProcessedAt())
                .fraudScore(claim.getFraudScore())
                .isValid(claim.getIsValid())
                .build();
    }
}
