package edu.university.iot.model.dtoModel;

import java.util.List;
import java.util.Map;

public class TrustAnalysisDto {
    private String deviceId;
    private Double currentTrustScore;
    private boolean trusted;
    private boolean quarantined;
    private List<TrustFactorDto> trustFactors;
    private String overallHealth;
    private String riskLevel;
    private List<String> recommendations;

    // Constructors
    public TrustAnalysisDto() {}

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Double getCurrentTrustScore() { return currentTrustScore; }
    public void setCurrentTrustScore(Double currentTrustScore) { this.currentTrustScore = currentTrustScore; }

    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }

    public boolean isQuarantined() { return quarantined; }
    public void setQuarantined(boolean quarantined) { this.quarantined = quarantined; }

    public List<TrustFactorDto> getTrustFactors() { return trustFactors; }
    public void setTrustFactors(List<TrustFactorDto> trustFactors) { this.trustFactors = trustFactors; }

    public String getOverallHealth() { return overallHealth; }
    public void setOverallHealth(String overallHealth) { this.overallHealth = overallHealth; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}