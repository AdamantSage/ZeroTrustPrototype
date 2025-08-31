// src/main/java/edu/university/iot/model/dtoModel/TrustAnalysisDto.java
package edu.university.iot.model.dtoModel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class TrustAnalysisDto {
    private String deviceId;
    private double currentTrustScore;
    private boolean trusted;
    private double trustThreshold;
    private Instant lastUpdated;
    
    // Historical context
    private Double averageTrustScore7Days;
    private Double averageTrustScore30Days;
    private String trendDirection; // IMPROVING, DEGRADING, STABLE
    private Integer trustScoreChanges24h;
    
    // Trust factors analysis
    private Map<String, Object> trustFactors;
    private Map<String, String> factorStatuses; // HIGH_RISK, MEDIUM_RISK, LOW_RISK, NO_DATA
    private Map<String, Double> factorWeights;
    
    // Risk assessment
    private String overallRiskLevel; // CRITICAL, HIGH, MEDIUM, LOW
    private List<String> riskIndicators;
    private List<String> positiveIndicators;
    
    // Recommendations
    private List<String> actionableRecommendations;
    private String nextReviewDate;
    
    // Performance metrics
    private Map<String, Integer> complianceMetrics;
    private Map<String, Integer> reliabilityMetrics;

    // Constructors
    public TrustAnalysisDto() {}

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public double getCurrentTrustScore() { return currentTrustScore; }
    public void setCurrentTrustScore(double currentTrustScore) { this.currentTrustScore = currentTrustScore; }

    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }

    public double getTrustThreshold() { return trustThreshold; }
    public void setTrustThreshold(double trustThreshold) { this.trustThreshold = trustThreshold; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public Double getAverageTrustScore7Days() { return averageTrustScore7Days; }
    public void setAverageTrustScore7Days(Double averageTrustScore7Days) { this.averageTrustScore7Days = averageTrustScore7Days; }

    public Double getAverageTrustScore30Days() { return averageTrustScore30Days; }
    public void setAverageTrustScore30Days(Double averageTrustScore30Days) { this.averageTrustScore30Days = averageTrustScore30Days; }

    public String getTrendDirection() { return trendDirection; }
    public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

    public Integer getTrustScoreChanges24h() { return trustScoreChanges24h; }
    public void setTrustScoreChanges24h(Integer trustScoreChanges24h) { this.trustScoreChanges24h = trustScoreChanges24h; }

    public Map<String, Object> getTrustFactors() { return trustFactors; }
    public void setTrustFactors(Map<String, Object> trustFactors) { this.trustFactors = trustFactors; }

    public Map<String, String> getFactorStatuses() { return factorStatuses; }
    public void setFactorStatuses(Map<String, String> factorStatuses) { this.factorStatuses = factorStatuses; }

    public Map<String, Double> getFactorWeights() { return factorWeights; }
    public void setFactorWeights(Map<String, Double> factorWeights) { this.factorWeights = factorWeights; }

    public String getOverallRiskLevel() { return overallRiskLevel; }
    public void setOverallRiskLevel(String overallRiskLevel) { this.overallRiskLevel = overallRiskLevel; }

    public List<String> getRiskIndicators() { return riskIndicators; }
    public void setRiskIndicators(List<String> riskIndicators) { this.riskIndicators = riskIndicators; }

    public List<String> getPositiveIndicators() { return positiveIndicators; }
    public void setPositiveIndicators(List<String> positiveIndicators) { this.positiveIndicators = positiveIndicators; }

    public List<String> getActionableRecommendations() { return actionableRecommendations; }
    public void setActionableRecommendations(List<String> actionableRecommendations) { this.actionableRecommendations = actionableRecommendations; }

    public String getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(String nextReviewDate) { this.nextReviewDate = nextReviewDate; }

    public Map<String, Integer> getComplianceMetrics() { return complianceMetrics; }
    public void setComplianceMetrics(Map<String, Integer> complianceMetrics) { this.complianceMetrics = complianceMetrics; }

    public Map<String, Integer> getReliabilityMetrics() { return reliabilityMetrics; }
    public void setReliabilityMetrics(Map<String, Integer> reliabilityMetrics) { this.reliabilityMetrics = reliabilityMetrics; }
}