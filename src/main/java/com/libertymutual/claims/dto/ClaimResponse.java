package com.libertymutual.claims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private String type;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status;
    private LocalDateTime processedAt;
    private Double fraudScore;
    private Boolean isValid;
}
