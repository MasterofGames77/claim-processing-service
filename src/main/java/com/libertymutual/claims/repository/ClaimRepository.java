package com.libertymutual.claims.repository;

import com.libertymutual.claims.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
    @Query("SELECT c.status, COUNT(c) FROM Claim c GROUP BY c.status")
    Map<String, Long> countByStatus();
    
    @Query("SELECT c.status, SUM(c.amount) FROM Claim c GROUP BY c.status")
    Map<String, java.math.BigDecimal> sumAmountByStatus();
}
