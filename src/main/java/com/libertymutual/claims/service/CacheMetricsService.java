package com.libertymutual.claims.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class CacheMetricsService {
    
    @Getter
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    
    @Getter
    private final AtomicLong cacheMissCount = new AtomicLong(0);
    
    public void recordCacheHit() {
        cacheHitCount.incrementAndGet();
    }
    
    public void recordCacheMiss() {
        cacheMissCount.incrementAndGet();
    }
    
    public void reset() {
        cacheHitCount.set(0);
        cacheMissCount.set(0);
    }
}
