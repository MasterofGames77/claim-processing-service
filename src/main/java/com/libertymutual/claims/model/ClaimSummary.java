package com.libertymutual.claims.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSummary {
    private Long totalClaims;
    private Map<String, Long> claimsByStatus;
    private BigDecimal totalAmount;
    private Map<String, BigDecimal> amountByStatus;
    private Long cacheHitCount;
    private Long cacheMissCount;
}
