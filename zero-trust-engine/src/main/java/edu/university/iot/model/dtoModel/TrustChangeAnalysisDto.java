package edu.university.iot.model.dtoModel;

import java.util.List;
import java.util.Map;

/**
 * DTO for detailed trust score change analysis
 */
public class TrustChangeAnalysisDto {
    private String deviceId;
    private int analysisPeriodHours;
    private int totalChanges;
    private double netScoreChange;
    private int improvingChanges;
    private int degradingChanges;
    private Map<String, Integer> factorImpacts;
    private String trend; // IMPROVING, DEGRADING, STABLE
    private List<String> criticalEvents;
    private List<String> patterns;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String summary;

    // Constructors
    public TrustChangeAnalysisDto() {}

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public int getAnalysisPeriodHours() { return analysisPeriodHours; }
    public void setAnalysisPeriodHours(int analysisPeriodHours) { this.analysisPeriodHours = analysisPeriodHours; }

    public int getTotalChanges() { return totalChanges; }
    public void setTotalChanges(int totalChanges) { this.totalChanges = totalChanges; }

    public double getNetScoreChange() { return netScoreChange; }
    public void setNetScoreChange(double netScoreChange) { this.netScoreChange = netScoreChange; }

    public int getImprovingChanges() { return improvingChanges; }
    public void setImprovingChanges(int improvingChanges) { this.improvingChanges = improvingChanges; }

    public int getDegradingChanges() { return degradingChanges; }
    public void setDegradingChanges(int degradingChanges) { this.degradingChanges = degradingChanges; }

    public Map<String, Integer> getFactorImpacts() { return factorImpacts; }
    public void setFactorImpacts(Map<String, Integer> factorImpacts) { this.factorImpacts = factorImpacts; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public List<String> getCriticalEvents() { return criticalEvents; }
    public void setCriticalEvents(List<String> criticalEvents) { this.criticalEvents = criticalEvents; }

    public List<String> getPatterns() { return patterns; }
    public void setPatterns(List<String> patterns) { this.patterns = patterns; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}