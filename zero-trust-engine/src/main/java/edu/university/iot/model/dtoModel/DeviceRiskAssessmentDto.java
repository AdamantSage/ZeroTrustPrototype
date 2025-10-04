package edu.university.iot.model.dtoModel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for comprehensive device risk assessment
 */
public class DeviceRiskAssessmentDto {
    private String deviceId;
    private double currentTrustScore;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String riskTrend; // IMPROVING, STABLE, DEGRADING
    private Instant lastAssessment;
    
    // Risk factors breakdown
    private Map<String, String> riskFactors; // factor -> status
    private List<String> activeThreats;
    private List<String> recommendations;
    
    // Recent activity indicators
    private int recentAnomalies;
    private int locationChanges;
    private boolean complianceIssues;
    private boolean identityIssues;
    
    // Predictive indicators
    private double predictedTrustScore24h;
    private String predictedRisk;
    private double confidenceLevel;

    // Constructors
    public DeviceRiskAssessmentDto() {}

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public double getCurrentTrustScore() { return currentTrustScore; }
    public void setCurrentTrustScore(double currentTrustScore) { this.currentTrustScore = currentTrustScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskTrend() { return riskTrend; }
    public void setRiskTrend(String riskTrend) { this.riskTrend = riskTrend; }

    public Instant getLastAssessment() { return lastAssessment; }
    public void setLastAssessment(Instant lastAssessment) { this.lastAssessment = lastAssessment; }

    public Map<String, String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(Map<String, String> riskFactors) { this.riskFactors = riskFactors; }

    public List<String> getActiveThreats() { return activeThreats; }
    public void setActiveThreats(List<String> activeThreats) { this.activeThreats = activeThreats; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public int getRecentAnomalies() { return recentAnomalies; }
    public void setRecentAnomalies(int recentAnomalies) { this.recentAnomalies = recentAnomalies; }

    public int getLocationChanges() { return locationChanges; }
    public void setLocationChanges(int locationChanges) { this.locationChanges = locationChanges; }

    public boolean isComplianceIssues() { return complianceIssues; }
    public void setComplianceIssues(boolean complianceIssues) { this.complianceIssues = complianceIssues; }

    public boolean isIdentityIssues() { return identityIssues; }
    public void setIdentityIssues(boolean identityIssues) { this.identityIssues = identityIssues; }

    public double getPredictedTrustScore24h() { return predictedTrustScore24h; }
    public void setPredictedTrustScore24h(double predictedTrustScore24h) { this.predictedTrustScore24h = predictedTrustScore24h; }

    public String getPredictedRisk() { return predictedRisk; }
    public void setPredictedRisk(String predictedRisk) { this.predictedRisk = predictedRisk; }

    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
}