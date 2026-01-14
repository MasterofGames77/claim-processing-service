package com.libertymutual.claims.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PerformanceMonitoringService {
    
    private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalResponseTimes = new ConcurrentHashMap<>();
    
    public void recordRequestTime(String endpoint, long durationMs) {
        requestCounts.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        totalResponseTimes.computeIfAbsent(endpoint, k -> new AtomicLong(0)).addAndGet(durationMs);
    }
    
    public PerformanceMetrics getMetrics() {
        double overallAverage = requestCounts.entrySet().stream()
                .mapToDouble(entry -> {
                    long count = entry.getValue().get();
                    long totalTime = totalResponseTimes.getOrDefault(entry.getKey(), new AtomicLong(0)).get();
                    return count > 0 ? (double) totalTime / count : 0.0;
                })
                .average()
                .orElse(0.0);
        
        long totalRequests = requestCounts.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        
        Map<String, Double> endpointAverages = new ConcurrentHashMap<>();
        requestCounts.forEach((endpoint, count) -> {
            long totalTime = totalResponseTimes.getOrDefault(endpoint, new AtomicLong(0)).get();
            double avg = count.get() > 0 ? (double) totalTime / count.get() : 0.0;
            endpointAverages.put(endpoint, avg);
        });
        
        return new PerformanceMetrics(overallAverage, totalRequests, endpointAverages);
    }
    
    public record PerformanceMetrics(
            double averageResponseTime,
            long totalRequests,
            Map<String, Double> endpointAverages
    ) {}
}
