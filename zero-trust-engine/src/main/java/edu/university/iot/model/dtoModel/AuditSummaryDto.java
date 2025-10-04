package edu.university.iot.model.dtoModel;

public class AuditSummaryDto {
        private long totalQuarantineActions;
        private long totalLocationChanges;
        private long totalFirmwareChecks;
        private long devicesWithIssues;

        // Getters and setters
        public long getTotalQuarantineActions() { return totalQuarantineActions; }
        public void setTotalQuarantineActions(long totalQuarantineActions) { this.totalQuarantineActions = totalQuarantineActions; }

        public long getTotalLocationChanges() { return totalLocationChanges; }
        public void setTotalLocationChanges(long totalLocationChanges) { this.totalLocationChanges = totalLocationChanges; }

        public long getTotalFirmwareChecks() { return totalFirmwareChecks; }
        public void setTotalFirmwareChecks(long totalFirmwareChecks) { this.totalFirmwareChecks = totalFirmwareChecks; }

        public long getDevicesWithIssues() { return devicesWithIssues; }
        public void setDevicesWithIssues(long devicesWithIssues) { this.devicesWithIssues = devicesWithIssues; }
    }
