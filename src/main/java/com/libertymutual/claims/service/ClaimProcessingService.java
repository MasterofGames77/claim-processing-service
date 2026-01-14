package com.libertymutual.claims.service;

import com.libertymutual.claims.model.Claim;
import com.libertymutual.claims.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimProcessingService {
    
    private final ClaimRepository claimRepository;
    private final Random random = new Random();
    
    @Async
    @Transactional
    public CompletableFuture<Void> processClaimAsync(@NonNull Long claimId) {
        log.info("Starting async processing for claim ID: {}", claimId);
        
        try {
            // Simulate processing time (validation, fraud scoring, etc.)
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 seconds
            
            Claim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found: " + claimId));
            
            // Simulate fraud scoring (0.0 to 1.0)
            double fraudScore = random.nextDouble();
            
            // Simulate validation (fraud score < 0.7 is valid)
            boolean isValid = fraudScore < 0.7;
            
            // Update claim with processing results
            claim.setProcessedAt(LocalDateTime.now());
            claim.setFraudScore(fraudScore);
            claim.setIsValid(isValid);
            
            // Update status based on validation
            if (!isValid) {
                claim.setStatus("REJECTED");
            } else if (fraudScore > 0.5) {
                claim.setStatus("REVIEW");
            }
            
            claimRepository.save(claim);
            
            log.info("Completed async processing for claim ID: {}. Fraud score: {}, Valid: {}", 
                    claimId, fraudScore, isValid);
            
            return CompletableFuture.completedFuture(null);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing interrupted for claim ID: {}", claimId, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Error processing claim ID: {}", claimId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
