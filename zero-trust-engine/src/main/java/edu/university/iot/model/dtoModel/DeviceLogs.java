// Model to reduce payload for frontend
package edu.university.iot.model.dtoModel;

import java.util.List;

import edu.university.iot.model.AnomalyLog;
import edu.university.iot.model.ComplianceLog;
import edu.university.iot.model.DeviceSession;
import edu.university.iot.model.FirmwareLog;
import edu.university.iot.model.IdentityLog;
import edu.university.iot.model.LocationNetworkChange;

public class DeviceLogs {
    private List<IdentityLog>     identityLogs;
    private List<FirmwareLog>     firmwareLogs;
    private List<AnomalyLog>      anomalyLogs;
    private List<ComplianceLog>   complianceLogs;
    private List<LocationNetworkChange> contextChanges;
    private DeviceSession         session;
    private double                trustScore;
    private boolean               trusted;


    public List<IdentityLog> getIdentityLogs() {
        return identityLogs;
    }
    public void setIdentityLogs(List<IdentityLog> identityLogs) {
        this.identityLogs = identityLogs;
    }
    public List<FirmwareLog> getFirmwareLogs() {
        return firmwareLogs;
    }
    public void setFirmwareLogs(List<FirmwareLog> firmwareLogs) {
        this.firmwareLogs = firmwareLogs;
    }
    public List<AnomalyLog> getAnomalyLogs() {
        return anomalyLogs;
    }
    public void setAnomalyLogs(List<AnomalyLog> anomalyLogs) {
        this.anomalyLogs = anomalyLogs;
    }
    public List<ComplianceLog> getComplianceLogs() {
        return complianceLogs;
    }
    public void setComplianceLogs(List<ComplianceLog> complianceLogs) {
        this.complianceLogs = complianceLogs;
    }
    public List<LocationNetworkChange> getContextChanges() {
        return contextChanges;
    }
    public void setContextChanges(List<LocationNetworkChange> contextChanges) {
        this.contextChanges = contextChanges;
    }
    public DeviceSession getSession() {
        return session;
    }
    public void setSession(DeviceSession session) {
        this.session = session;
    }
    public double getTrustScore() {
        return trustScore;
    }
    public void setTrustScore(double trustScore) {
        this.trustScore = trustScore;
    }
    public boolean isTrusted() {
        return trusted;
    }
    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

}
