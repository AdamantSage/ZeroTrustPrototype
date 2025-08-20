package edu.university.iot.model.dtoModel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for comprehensive trust analysis of a device
 */
public class TrustAnalysisDto {
    private String deviceId;
    private double currentTrustScore;
    private boolean isTrusted;
    private double trustThreshold;
    private Instant lastUpdated;
    
    // Trust factors breakdown
    private Map<String, Object> trustFactors; // factor -> detailed analysis
    private Map<String, String> factorStatuses; // factor -> LOW_RISK/MEDIUM_RISK/HIGH_RISK
    private Map<String, Double> factorWeights; // factor -> weight in scoring
    
    // Historical context
    private double averageTrustScore7Days;
    private double averageTrustScore30Days;
    private String trendDirection; // IMPROVING, STABLE, DEGRADING
    private int trustScoreChanges24h;
    
    // Risk assessment
    private String overallRiskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private List<String> riskIndicators;
    private List<String> positiveIndicators;
    
    // Recommendations
    private List<String> actionableRecommendations;
    private String nextReviewDate;
    
    // Performance metrics
    private Map<String, Integer> complianceMetrics; // policy -> compliance percentage
    private Map<String, Integer> reliabilityMetrics; // metric -> score

    // Constructors
    public TrustAnalysisDto() {}

    public TrustAnalysisDto(String deviceId, double currentTrustScore) {
        this.deviceId = deviceId;
        this.currentTrustScore = currentTrustScore;
        this.lastUpdated = Instant.now();
    }

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public double getCurrentTrustScore() { return currentTrustScore; }
    public void setCurrentTrustScore(double currentTrustScore) { this.currentTrustScore = currentTrustScore; }

    public boolean isTrusted() { return isTrusted; }
    public void setTrusted(boolean trusted) { isTrusted = trusted; }

    public double getTrustThreshold() { return trustThreshold; }
    public void setTrustThreshold(double trustThreshold) { this.trustThreshold = trustThreshold; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public Map<String, Object> getTrustFactors() { return trustFactors; }
    public void setTrustFactors(Map<String, Object> trustFactors) { this.trustFactors = trustFactors; }

    public Map<String, String> getFactorStatuses() { return factorStatuses; }
    public void setFactorStatuses(Map<String, String> factorStatuses) { this.factorStatuses = factorStatuses; }

    public Map<String, Double> getFactorWeights() { return factorWeights; }
    public void setFactorWeights(Map<String, Double> factorWeights) { this.factorWeights = factorWeights; }

    public double getAverageTrustScore7Days() { return averageTrustScore7Days; }
    public void setAverageTrustScore7Days(double averageTrustScore7Days) { this.averageTrustScore7Days = averageTrustScore7Days; }

    public double getAverageTrustScore30Days() { return averageTrustScore30Days; }
    public void setAverageTrustScore30Days(double averageTrustScore30Days) { this.averageTrustScore30Days = averageTrustScore30Days; }

    public String getTrendDirection() { return trendDirection; }
    public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

    public int getTrustScoreChanges24h() { return trustScoreChanges24h; }
    public void setTrustScoreChanges24h(int trustScoreChanges24h) { this.trustScoreChanges24h = trustScoreChanges24h; }

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