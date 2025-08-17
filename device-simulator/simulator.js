/**
 * Unified Realistic Cybersecurity Device Simulator
 * Single complete implementation demonstrating genuine security improvement
 */

const { Client, Message } = require('azure-iot-device');
const Protocol = require('azure-iot-device-mqtt').Mqtt;

/* === DEVICE CONNECTIONS === */
const DEVICE_CONNECTIONS = {
  laptop001: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop001;SharedAccessKey=hDhxjBVTDGBXjhNk+Kmo/GW5IZYyK1jWK5WjjtVJS1E=',
  laptop002: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop002;SharedAccessKey=bbqdM7Q+HRY20mo2djn7H6dc/zmu5h+rw8CGBo9iDbU=',
  laptop003: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop003;SharedAccessKey=r6TOvCuGn6pFlyzLxeyYcnwK89MsHW9xyFG+io/5pJU='
};

const CAMPUS_LOCATIONS = {
  'Library-Floor1': { lat: -26.6876, lng: 27.0936, subnet: '10.1.1' },
  'Library-Floor2': { lat: -26.6877, lng: 27.0937, subnet: '10.1.2' },
  'Lecture-Hall-A': { lat: -26.6880, lng: 27.0940, subnet: '10.1.3' },
  'Lecture-Hall-B': { lat: -26.6882, lng: 27.0942, subnet: '10.1.4' },
  'Computer-Lab-1': { lat: -26.6875, lng: 27.0935, subnet: '10.1.5' },
  'Computer-Lab-2': { lat: -26.6873, lng: 27.0933, subnet: '10.1.6' },
  'Student-Center': { lat: -26.6878, lng: 27.0938, subnet: '10.1.7' },
  'Admin-Building': { lat: -26.6885, lng: 27.0945, subnet: '10.1.8' },
  'Cafeteria': { lat: -26.6879, lng: 27.0939, subnet: '10.1.9' },
  'Off-Campus-Home': { lat: -26.7000, lng: 27.1000, subnet: '192.168.1' },
  'Off-Campus-Cafe': { lat: -26.6950, lng: 27.0900, subnet: '192.168.43' }
};

/* === REALISTIC DEVICE PROFILES === */
const DEVICE_PROFILES = {
  laptop001: {
    id: 'laptop001',
    type: 'ENTERPRISE_MANAGED',
    
    // Security characteristics
    patchCompliance: 0.95,           // 95% chance to apply patches
    maintenanceFrequency: 'weekly',  // weekly maintenance schedule
    learningEnabled: true,           // AI/ML learning enabled
    baseSecurityScore: 0.05,         // starts with good security (low anomaly)
    securityAwareness: 0.9,          // high security awareness
    
    // Behavioral patterns
    preferredLocations: ['Library-Floor1', 'Library-Floor2', 'Computer-Lab-1'],
    workingHours: { start: 8, end: 17 },
    locationChangeFrequency: 3600000, // 1 hour
    
    // Technical specifications
    limits: { cpu: 85, memory: 90, network: 1000 },
    complianceRate: 0.95,
    expectedVersion: '1.2.0',
    
    // Vulnerability management
    vulnerabilityDiscoveryRate: 0.02,
    incidentResponseTime: 300        // 5 minutes
  },
  
  laptop002: {
    id: 'laptop002',
    type: 'BYOD_STUDENT',
    
    patchCompliance: 0.70,
    maintenanceFrequency: 'monthly',
    learningEnabled: true,
    baseSecurityScore: 0.15,
    securityAwareness: 0.6,
    
    preferredLocations: ['Student-Center', 'Off-Campus-Cafe', 'Off-Campus-Home', 'Library-Floor2'],
    workingHours: { start: 10, end: 22 },
    locationChangeFrequency: 900000, // 15 minutes
    
    limits: { cpu: 80, memory: 85, network: 800 },
    complianceRate: 0.60,
    expectedVersion: '1.3.5',
    
    vulnerabilityDiscoveryRate: 0.08,
    incidentResponseTime: 1800       // 30 minutes
  },
  
  laptop003: {
    id: 'laptop003',
    type: 'LEGACY_SYSTEM',
    
    patchCompliance: 0.40,
    maintenanceFrequency: 'reactive',
    learningEnabled: false,          // no learning capabilities
    baseSecurityScore: 0.35,
    securityAwareness: 0.3,
    
    preferredLocations: ['Computer-Lab-2', 'Admin-Building'],
    workingHours: { start: 6, end: 14 },
    locationChangeFrequency: 600000, // 10 minutes
    
    limits: { cpu: 75, memory: 80, network: 600 },
    complianceRate: 0.40,
    expectedVersion: '1.0.3',
    
    vulnerabilityDiscoveryRate: 0.15,
    incidentResponseTime: 3600       // 1 hour
  }
};

/* === SINGLE UNIFIED SIMULATOR CLASS === */
class RealisticCyberSecuritySimulator {
  constructor({ id, profile, campusLocations }) {
    this.id = id;
    this.profile = profile;
    this.campusLocations = campusLocations;
    
    // IoT Client
    this.client = null;
    this.running = false;
    
    // === SECURITY STATE TRACKING ===
    this.currentSecurityScore = profile.baseSecurityScore;
    
    // Patch management state
    this.patchLevels = {
      critical: Math.random() < profile.patchCompliance ? 1.0 : 0.6 + Math.random() * 0.3,
      security: Math.random() < profile.patchCompliance ? 1.0 : 0.5 + Math.random() * 0.4,
      feature: Math.random() < (profile.patchCompliance * 0.8) ? 1.0 : 0.3 + Math.random() * 0.5
    };
    this.lastPatchUpdate = Date.now() - (Math.random() * 7 * 24 * 60 * 60 * 1000);
    
    // Maintenance tracking
    this.lastMaintenance = Date.now() - (Math.random() * 14 * 24 * 60 * 60 * 1000);
    this.maintenanceHistory = [];
    
    // Learning and adaptation
    this.learningIterations = 0;
    this.adaptationBonus = 0;
    this.behaviorPatterns = new Map();
    this.learningHistory = [];
    
    // Compliance tracking
    this.complianceStreak = 0;
    this.complianceHistory = [];
    
    // Vulnerability management
    this.vulnerabilities = this.generateInitialVulnerabilities();
    this.lastVulnerabilityScan = Date.now();
    
    // === OPERATIONAL STATE ===
    this.currentLocation = profile.preferredLocations[Math.floor(Math.random() * profile.preferredLocations.length)];
    this.lastLocationChange = Date.now();
    this.locationHistory = [];
    
    this.sessionStart = Date.now();
    this.consecutiveAnomalies = 0;
    this.suspiciousActivityScore = Math.floor(Math.random() * 20);
    
    console.log(`[${this.id}] 🚀 Initialized ${this.profile.type} - Base Security: ${this.currentSecurityScore.toFixed(3)}`);
  }
  
  /* === PATCH MANAGEMENT SYSTEM === */
  generateInitialVulnerabilities() {
    const count = Math.floor(Math.random() * 5) + 1;
    return Array.from({length: count}, (_, i) => ({
      id: `CVE-2024-${Math.floor(Math.random() * 9000) + 1000}`,
      severity: ['low', 'medium', 'high', 'critical'][Math.floor(Math.random() * 4)],
      discoveredAt: Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000,
      patched: false
    }));
  }
  
  processPatchManagement() {
    const now = Date.now();
    const daysSinceLastPatch = (now - this.lastPatchUpdate) / (24 * 60 * 60 * 1000);
    
    // Daily patch checking
    if (daysSinceLastPatch >= 1) {
      const shouldPatch = Math.random() < this.profile.patchCompliance;
      
      if (shouldPatch) {
        let totalImprovement = 0;
        let patchesApplied = 0;
        
        // Apply critical patches (highest priority)
        if (this.patchLevels.critical < 1.0 && Math.random() < 0.9) {
          const improvement = this.applyCriticalPatches();
          totalImprovement += improvement;
          patchesApplied++;
        }
        
        // Apply security patches
        if (this.patchLevels.security < 1.0 && Math.random() < 0.7) {
          const improvement = this.applySecurityPatches();
          totalImprovement += improvement;
          patchesApplied++;
        }
        
        // Apply feature patches
        if (this.patchLevels.feature < 1.0 && Math.random() < 0.5) {
          const improvement = this.applyFeaturePatches();
          totalImprovement += improvement;
          patchesApplied++;
        }
        
        if (totalImprovement > 0) {
          this.currentSecurityScore = Math.max(0.01, this.currentSecurityScore - totalImprovement);
          this.lastPatchUpdate = now;
          
          // Patch some vulnerabilities
          this.patchVulnerabilities(patchesApplied);
          
          console.log(`[${this.id}] 📦 Applied ${patchesApplied} patches - Security improved by ${totalImprovement.toFixed(3)}`);
        }
      } else {
        // Security degrades without patches
        const degradation = Math.min(0.01, daysSinceLastPatch * 0.005);
        this.currentSecurityScore = Math.min(0.8, this.currentSecurityScore + degradation);
        
        if (daysSinceLastPatch > 7) {
          console.log(`[${this.id}] ⚠️  ${Math.floor(daysSinceLastPatch)} days without patches - Security degrading`);
        }
      }
    }
  }
  
  applyCriticalPatches() {
    const oldLevel = this.patchLevels.critical;
    this.patchLevels.critical = Math.min(1.0, this.patchLevels.critical + 0.4);
    const improvement = (this.patchLevels.critical - oldLevel) * 0.15; // 15% improvement per level
    return improvement;
  }
  
  applySecurityPatches() {
    const oldLevel = this.patchLevels.security;
    this.patchLevels.security = Math.min(1.0, this.patchLevels.security + 0.3);
    const improvement = (this.patchLevels.security - oldLevel) * 0.08; // 8% improvement per level
    return improvement;
  }
  
  applyFeaturePatches() {
    const oldLevel = this.patchLevels.feature;
    this.patchLevels.feature = Math.min(1.0, this.patchLevels.feature + 0.2);
    const improvement = (this.patchLevels.feature - oldLevel) * 0.03; // 3% improvement per level
    return improvement;
  }
  
  patchVulnerabilities(patchCount) {
    // Patch some vulnerabilities based on patches applied
    const vulnersToPatch = Math.min(patchCount, this.vulnerabilities.filter(v => !v.patched).length);
    let patched = 0;
    
    for (let vuln of this.vulnerabilities) {
      if (!vuln.patched && patched < vulnersToPatch) {
        if (vuln.severity === 'critical' && Math.random() < 0.9) {
          vuln.patched = true;
          patched++;
        } else if (Math.random() < 0.6) {
          vuln.patched = true;
          patched++;
        }
      }
    }
  }
  
  /* === MAINTENANCE CYCLES === */
  processMaintenanceCycles() {
    const now = Date.now();
    const daysSinceMaintenance = (now - this.lastMaintenance) / (24 * 60 * 60 * 1000);
    const frequency = this.profile.maintenanceFrequency;
    
    let shouldPerformMaintenance = false;
    
    if (frequency === 'weekly' && daysSinceMaintenance >= 7) {
      shouldPerformMaintenance = Math.random() < 0.8;
    } else if (frequency === 'monthly' && daysSinceMaintenance >= 30) {
      shouldPerformMaintenance = Math.random() < 0.6;
    } else if (frequency === 'reactive' && this.currentSecurityScore > 0.4) {
      shouldPerformMaintenance = Math.random() < 0.3;
    }
    
    if (shouldPerformMaintenance) {
      const maintenanceImprovement = this.performMaintenance();
      this.lastMaintenance = now;
      this.maintenanceHistory.push({
        timestamp: now,
        improvement: maintenanceImprovement,
        type: frequency
      });
      
      console.log(`[${this.id}] 🔧 Performed ${frequency} maintenance - Security improved by ${maintenanceImprovement.toFixed(3)}`);
    }
  }
  
  performMaintenance() {
    // Maintenance activities
    const baseImprovement = 0.08 + Math.random() * 0.04; // 8-12% improvement
    this.currentSecurityScore = Math.max(0.01, this.currentSecurityScore - baseImprovement);
    
    // Clean up some vulnerabilities
    this.vulnerabilities = this.vulnerabilities.filter(v => v.patched || Math.random() < 0.7);
    
    // Reset negative counters
    this.consecutiveAnomalies = Math.max(0, this.consecutiveAnomalies - 3);
    this.suspiciousActivityScore = Math.max(0, this.suspiciousActivityScore - 15);
    
    return baseImprovement;
  }
  
  /* === LEARNING AND ADAPTATION === */
  processLearningSystem() {
    if (!this.profile.learningEnabled) return;
    
    this.learningIterations++;
    
    // Gradual learning improvement every 20 cycles
    if (this.learningIterations % 20 === 0) {
      const learningImprovement = (0.01 + Math.random() * 0.01) * this.profile.securityAwareness;
      this.adaptationBonus += learningImprovement;
      console.log(`[${this.id}] 🧠 ML adaptation - Cumulative learning: ${this.adaptationBonus.toFixed(3)}`);
    }
    
    // Pattern recognition bonus every 50 cycles
    if (this.learningIterations > 50 && this.learningIterations % 50 === 0) {
      const patternBonus = 0.02 * this.profile.securityAwareness;
      this.adaptationBonus += patternBonus;
      console.log(`[${this.id}] 🔍 Pattern recognition bonus applied`);
    }
    
    // Store learning history
    this.learningHistory.push({
      iteration: this.learningIterations,
      securityScore: this.currentSecurityScore,
      adaptationBonus: this.adaptationBonus,
      timestamp: Date.now()
    });
    
    // Keep history manageable
    if (this.learningHistory.length > 100) {
      this.learningHistory.shift();
    }
  }
  
  /* === COMPLIANCE TRACKING === */
  processComplianceTracking(isCompliant) {
    if (isCompliant) {
      this.complianceStreak++;
      
      // Reward sustained compliance
      if (this.complianceStreak >= 10) {
        const complianceBonus = Math.min(0.05, this.complianceStreak * 0.002);
        this.currentSecurityScore = Math.max(0.01, this.currentSecurityScore - complianceBonus);
        
        if (this.complianceStreak % 20 === 0) {
          console.log(`[${this.id}] ✅ Extended compliance streak (${this.complianceStreak}) - Security bonus applied`);
        }
      }
    } else {
      if (this.complianceStreak > 0) {
        const penalty = this.currentSecurityScore * 0.1;
        this.currentSecurityScore = Math.min(0.8, this.currentSecurityScore + penalty);
        console.log(`[${this.id}] ❌ Compliance streak broken - Security penalty applied`);
      }
      this.complianceStreak = 0;
    }
    
    this.complianceHistory.push({
      timestamp: Date.now(),
      compliant: isCompliant,
      streak: this.complianceStreak
    });
    
    if (this.complianceHistory.length > 50) {
      this.complianceHistory.shift();
    }
  }
  
  /* === VULNERABILITY DISCOVERY === */
  processVulnerabilityDiscovery() {
    // Periodic vulnerability discovery
    if (Math.random() < this.profile.vulnerabilityDiscoveryRate / 100) {
      const newVuln = {
        id: `CVE-2024-${Math.floor(Math.random() * 9000) + 1000}`,
        severity: ['low', 'medium', 'high', 'critical'][Math.floor(Math.random() * 4)],
        discoveredAt: Date.now(),
        patched: false
      };
      
      this.vulnerabilities.push(newVuln);
      
      // Impact on security score based on severity
      const impact = {low: 0.02, medium: 0.05, high: 0.1, critical: 0.2}[newVuln.severity];
      this.currentSecurityScore = Math.min(0.8, this.currentSecurityScore + impact);
    }
  }
  
  /* === LOCATION AND BEHAVIOR === */
  updateLocationBehavior() {
    const now = Date.now();
    const hour = new Date().getHours();
    const workHours = this.profile.workingHours;
    
    // Realistic location changes
    if (Math.random() < 0.1) { // 10% chance per cycle
      let newLocation = this.currentLocation;
      
      if (hour >= workHours.start && hour <= workHours.end) {
        // Work hours - prefer campus locations
        const campusLocs = this.profile.preferredLocations.filter(loc => !loc.includes('Off-Campus'));
        if (campusLocs.length > 0) {
          newLocation = campusLocs[Math.floor(Math.random() * campusLocs.length)];
        }
      } else {
        // Off hours - might go off-campus
        if (Math.random() < 0.4) {
          const offCampusLocs = ['Off-Campus-Home', 'Off-Campus-Cafe'];
          newLocation = offCampusLocs[Math.floor(Math.random() * offCampusLocs.length)];
        }
      }
      
      if (newLocation !== this.currentLocation) {
        this.locationHistory.push({
          from: this.currentLocation,
          to: newLocation,
          timestamp: now,
          coordinates: this.campusLocations[newLocation] || null
        });
        
        if (this.locationHistory.length > 20) {
          this.locationHistory.shift();
        }
        
        this.currentLocation = newLocation;
        this.lastLocationChange = now;
      }
    }
    
    return this.currentLocation;
  }
  
  /* === TELEMETRY GENERATION === */
  generateRealisticResourceUsage() {
    const hour = new Date().getHours();
    const workHours = this.profile.workingHours;
    const limits = this.profile.limits;
    
    // Base load varies by time and device type
    let baseLoad = 0.3;
    if (hour >= workHours.start && hour <= workHours.end) {
      baseLoad = 0.6; // Higher during work hours
    }
    
    // Device type adjustments
    if (this.profile.type === 'LEGACY_SYSTEM') {
      baseLoad *= 1.4; // Less efficient
    } else if (this.profile.type === 'ENTERPRISE_MANAGED') {
      baseLoad *= 0.8; // More efficient
    }
    
    // Security posture affects efficiency
    baseLoad += this.currentSecurityScore * 0.2;
    
    return {
      cpuUsage: parseFloat(Math.max(5, Math.min(100, baseLoad * limits.cpu + (Math.random() - 0.5) * 25)).toFixed(2)),
      memoryUsage: parseFloat(Math.max(10, Math.min(100, baseLoad * limits.memory + (Math.random() - 0.5) * 20)).toFixed(2)),
      networkTrafficVolume: parseFloat(Math.max(50, baseLoad * limits.network + Math.random() * 300).toFixed(2))
    };
  }
  
  assessCompliance(telemetryData) {
    const factors = {
      patches: this.getPatchStatus() === 'Up-to-date',
      resources: telemetryData.cpuUsage < this.profile.limits.cpu * 0.9,
      certificates: telemetryData.certificateValid,
      firmware: telemetryData.firmwareVersion === this.profile.expectedVersion,
      location: this.isAuthorizedLocation(),
      workingHours: this.isDuringWorkingHours()
    };
    
    const compliantCount = Object.values(factors).filter(Boolean).length;
    const totalFactors = Object.keys(factors).length;
    
    return compliantCount / totalFactors >= 0.7; // 70% threshold
  }
  
  /* === UTILITY METHODS === */
  getPatchStatus() {
    const avg = (this.patchLevels.critical + this.patchLevels.security + this.patchLevels.feature) / 3;
    if (avg >= 0.95) return 'Up-to-date';
    if (avg >= 0.8) return 'Mostly-updated';
    return 'Outdated';
  }
  
  isDuringWorkingHours() {
    const hour = new Date().getHours();
    return hour >= this.profile.workingHours.start && hour <= this.profile.workingHours.end;
  }
  
  isAuthorizedLocation() {
    if (this.profile.type === 'ENTERPRISE_MANAGED') {
      return !this.currentLocation.includes('Off-Campus');
    }
    return true; // BYOD allowed anywhere
  }
  
  generateIPAddress(location) {
    const loc = this.campusLocations[location];
    if (!loc) return `10.1.0.${Math.floor(Math.random() * 254) + 1}`;
    return `${loc.subnet}.${Math.floor(Math.random() * 254) + 1}`;
  }
  
  /* === MAIN TELEMETRY GENERATION === */
  generateTelemetry() {
    // Process all security improvement mechanisms
    this.processPatchManagement();
    this.processMaintenanceCycles();
    this.processLearningSystem();
    this.processVulnerabilityDiscovery();
    
    // Update location and generate resources
    const location = this.updateLocationBehavior();
    const coords = this.campusLocations[location] || null;
    const resources = this.generateRealisticResourceUsage();
    
    // Calculate final anomaly score
    let anomalyScore = this.currentSecurityScore;
    
    // Apply learning adaptation bonus
    anomalyScore = Math.max(0.01, anomalyScore - this.adaptationBonus);
    
    // Factor in resource anomalies
    if (resources.cpuUsage > this.profile.limits.cpu * 0.9) anomalyScore += 0.05;
    if (resources.memoryUsage > this.profile.limits.memory * 0.9) anomalyScore += 0.03;
    if (resources.networkTrafficVolume > this.profile.limits.network * 0.9) anomalyScore += 0.02;
    
    // Working hours factor
    if (!this.isDuringWorkingHours()) anomalyScore += 0.02;
    
    // Vulnerability factor
    const criticalVulns = this.vulnerabilities.filter(v => v.severity === 'critical' && !v.patched).length;
    anomalyScore += criticalVulns * 0.05;
    
    anomalyScore = Math.max(0, Math.min(1, parseFloat(anomalyScore.toFixed(3))));
    
    // Generate other telemetry fields
    const certificateValid = Math.random() < this.profile.complianceRate;
    const firmwareVersion = Math.random() < this.profile.complianceRate ? 
      this.profile.expectedVersion : '1.0.0';
    const malwareDetected = Math.random() < (anomalyScore * 0.1);
    
    const telemetryData = {
      deviceId: this.id,
      certificateValid,
      patchStatus: this.getPatchStatus(),
      firmwareVersion,
      ipAddress: this.generateIPAddress(location),
      location,
      coordinates: coords,
      cpuUsage: resources.cpuUsage,
      memoryUsage: resources.memoryUsage,
      networkTrafficVolume: resources.networkTrafficVolume,
      anomalyScore,
      malwareSignatureDetected: malwareDetected,
      sessionDuration: Math.floor((Date.now() - this.sessionStart) / 1000),
      suspiciousActivityScore: this.suspiciousActivityScore,
      consecutiveAnomalies: this.consecutiveAnomalies,
      deviceProfile: this.profile.type,
      timestamp: new Date().toISOString()
    };
    
    // Process compliance
    const isCompliant = this.assessCompliance(telemetryData);
    this.processComplianceTracking(isCompliant);
    
    // Update counters
    if (anomalyScore > 0.3) {
      this.consecutiveAnomalies++;
      this.suspiciousActivityScore = Math.min(100, this.suspiciousActivityScore + Math.floor(anomalyScore * 10));
    } else {
      if (this.consecutiveAnomalies > 0 && Math.random() < 0.6) {
        this.consecutiveAnomalies = Math.max(0, this.consecutiveAnomalies - 1);
      }
      this.suspiciousActivityScore = Math.max(0, this.suspiciousActivityScore - 2);
    }
    
    return telemetryData;
  }
  
  // Public method to attach IoT client
  attachClient(client) {
    this.client = client;
  }
  
  // Public method for forced patch updates
  forcePatchUpdate() {
    this.lastPatchUpdate = 0;
  }
  
  // Public method for forced maintenance
  forceMaintenance() {
    this.lastMaintenance = 0;
  }
  
  // Public method to get detailed status
  getDetailedStatus() {
    return {
      securityScore: this.currentSecurityScore,
      patchLevels: this.patchLevels,
      patchStatus: this.getPatchStatus(),
      complianceStreak: this.complianceStreak,
      adaptationBonus: this.adaptationBonus,
      vulnerabilityCount: this.vulnerabilities.length,
      criticalVulnerabilities: this.vulnerabilities.filter(v => v.severity === 'critical' && !v.patched).length,
      daysSinceLastPatch: Math.floor((Date.now() - this.lastPatchUpdate) / (24 * 60 * 60 * 1000)),
      daysSinceLastMaintenance: Math.floor((Date.now() - this.lastMaintenance) / (24 * 60 * 60 * 1000)),
      currentLocation: this.currentLocation
    };
  }
}

/* === CREATE AND RUN SIMULATORS === */
const simulators = Object.keys(DEVICE_CONNECTIONS).map(deviceId => {
  const profile = DEVICE_PROFILES[deviceId];
  if (!profile) {
    console.error(`❌ No profile found for device ${deviceId}`);
    return null;
  }
  
  const sim = new RealisticCyberSecuritySimulator({
    id: deviceId,
    profile: profile,
    campusLocations: CAMPUS_LOCATIONS
  });
  
  // Attach Azure IoT client
  try {
    const client = Client.fromConnectionString(DEVICE_CONNECTIONS[deviceId], Protocol);
    client.open(err => {
      if (err) {
        console.error(`[${deviceId}] ❌ Connection error:`, err.message);
      } else {
        console.log(`[${deviceId}] ✅ Connected to Azure IoT Hub`);
      }
    });
    sim.attachClient(client);
  } catch (e) {
    console.error(`[${deviceId}] ❌ Failed to create client:`, e.message);
  }
  
  return sim;
}).filter(Boolean);

/* === TELEMETRY LOOP === */
const TELEMETRY_INTERVAL = 5000; // 5 seconds

const sendTelemetry = () => {
  simulators.forEach(sim => {
    if (!sim.client) return;
    
    const telemetryData = sim.generateTelemetry();
    const message = new Message(JSON.stringify(telemetryData));
    
    // Add message properties for routing
    try {
      if (telemetryData.suspiciousActivityScore > 60) {
        message.properties.add('alert', 'suspicious-activity');
      }
      if (telemetryData.anomalyScore > 0.5) {
        message.properties.add('priority', 'high');
        message.properties.add('alert', 'high-anomaly');
      }
      if (telemetryData.consecutiveAnomalies > 3) {
        message.properties.add('priority', 'critical');
        message.properties.add('alert', 'potential-compromise');
      }
    } catch (e) {
      // Ignore property errors
    }
    
    sim.client.sendEvent(message, (err) => {
      if (err) {
        console.error(`[${sim.id}] ❌ Send error:`, err.message);
      } else {
        // Determine security status with emojis
        const securityStatus = telemetryData.anomalyScore < 0.15 ? '🟢 SECURE' : 
                              telemetryData.anomalyScore < 0.35 ? '🟡 MODERATE' : 
                              telemetryData.anomalyScore < 0.6 ? '🟠 ELEVATED' : '🔴 HIGH RISK';
        
        const coords = telemetryData.coordinates ? 
          `${telemetryData.coordinates.lat.toFixed(4)}, ${telemetryData.coordinates.lng.toFixed(4)}` : 'N/A';
        
        const alerts = [];
        if (telemetryData.suspiciousActivityScore > 60) alerts.push('SUSPICIOUS');
        if (telemetryData.consecutiveAnomalies > 3) alerts.push('COMPROMISED');
        if (telemetryData.malwareSignatureDetected) alerts.push('MALWARE');
        
        const alertStr = alerts.length > 0 ? ` [${alerts.join(', ')}]` : '';
        
        console.log(`[${sim.id}] 📡 ${telemetryData.location} (${coords}) | ${securityStatus} | Anomaly: ${telemetryData.anomalyScore} | ${telemetryData.patchStatus} | Compliance: ${sim.complianceStreak}${alertStr}`);
      }
    });
  });
};

// Start telemetry loop
const intervalHandle = setInterval(sendTelemetry, TELEMETRY_INTERVAL);

/* === GRACEFUL SHUTDOWN === */
function shutdown() {
  console.log('\n🔄 Shutting down unified realistic cybersecurity simulator...');
  clearInterval(intervalHandle);
  
  simulators.forEach(sim => {
    if (sim.client) {
      try {
        sim.client.close(() => {
          console.log(`[${sim.id}] 🔌 Disconnected from IoT Hub`);
        });
      } catch (e) {
        // Ignore cleanup errors
      }
    }
  });
  
  setTimeout(() => {
    console.log('✅ Simulator shutdown complete');
    process.exit(0);
  }, 2000);
}

process.on('SIGINT', shutdown);
process.on('SIGTERM', shutdown);

/* === INTERACTIVE COMMANDS === */
const readline = require('readline').createInterface({
  input: process.stdin,
  output: process.stdout
});

console.log('\n🔐 Unified Realistic Cybersecurity Simulator Started');
console.log('=' .repeat(70));
console.log('📊 Device Profiles:');
console.log('  🏢 laptop001: Enterprise Managed (95% patch compliance, weekly maintenance, ML enabled)');
console.log('  🎓 laptop002: BYOD Student (70% patch compliance, monthly maintenance, ML enabled)');
console.log('  🏛️  laptop003: Legacy System (40% patch compliance, reactive maintenance, no ML)');
console.log('');
console.log('🚀 Features Demonstrated:');
console.log('  • Patches genuinely improve security over time');
console.log('  • Good maintenance reduces actual risk');  
console.log('  • Consistent compliance is rewarded');
console.log('  • Learning systems adapt and improve');
console.log('');
console.log('💡 Commands: status | detailed | patch <deviceId> | maintenance <deviceId> | help | exit');
console.log('=' .repeat(70));

readline.on('line', (line) => {
  const parts = line.trim().split(/\s+/);
  const command = parts[0].toLowerCase();
  
  if (command === 'status') {
    console.log('\n📊 Quick Device Status:');
    console.log('-'.repeat(50));
    simulators.forEach(sim => {
      const status = sim.getDetailedStatus();
      const securityIcon = status.securityScore < 0.15 ? '🟢' : status.securityScore < 0.35 ? '🟡' : status.securityScore < 0.6 ? '🟠' : '🔴';
      
      console.log(`[${sim.id}] ${securityIcon} Security: ${status.securityScore.toFixed(3)} | Patches: ${status.patchStatus} | Compliance: ${status.complianceStreak} | Location: ${status.currentLocation}`);
    });
    console.log('');
    
  } else if (command === 'detailed') {
    console.log('\n📈 Detailed Device Analysis:');
    console.log('='.repeat(80));
    simulators.forEach(sim => {
      const status = sim.getDetailedStatus();
      const patchLevels = `Critical: ${(status.patchLevels.critical * 100).toFixed(0)}%, Security: ${(status.patchLevels.security * 100).toFixed(0)}%, Feature: ${(status.patchLevels.feature * 100).toFixed(0)}%`;
      
      console.log(`\n🖥️  [${sim.id}] - ${sim.profile.type}`);
      console.log(`   🛡️  Security Score: ${status.securityScore.toFixed(3)} (lower is better)`);
      console.log(`   📦 Patch Levels: ${patchLevels}`);
      console.log(`   🔧 Last Maintenance: ${status.daysSinceLastMaintenance} days ago`);
      console.log(`   🧠 Learning Bonus: ${status.adaptationBonus.toFixed(3)}`);
      console.log(`   ✅ Compliance Streak: ${status.complianceStreak} cycles`);
      console.log(`   🚨 Vulnerabilities: ${status.vulnerabilityCount} total, ${status.criticalVulnerabilities} critical`);
      console.log(`   📍 Location: ${status.currentLocation}`);
    });
    console.log('\n');
    
  } else if (command === 'patch' && parts[1]) {
    const deviceId = parts[1];
    const sim = simulators.find(s => s.id === deviceId);
    if (sim) {
      sim.forcePatchUpdate();
      console.log(`🔄 Forced patch update for ${deviceId} - will apply on next telemetry cycle`);
    } else {
      console.log('❌ Device not found. Available devices: laptop001, laptop002, laptop003');
    }
    
  } else if (command === 'maintenance' && parts[1]) {
    const deviceId = parts[1];
    const sim = simulators.find(s => s.id === deviceId);
    if (sim) {
      sim.forceMaintenance();
      console.log(`🔧 Forced maintenance for ${deviceId} - will perform on next telemetry cycle`);
    } else {
      console.log('❌ Device not found. Available devices: laptop001, laptop002, laptop003');
    }
    
  } else if (command === 'help') {
    console.log('\n💡 Available Commands:');
    console.log('  📊 status              - Show quick status overview of all devices');
    console.log('  📈 detailed            - Show comprehensive analysis of all devices');
    console.log('  📦 patch <deviceId>    - Force immediate patch update for specific device');
    console.log('  🔧 maintenance <id>    - Force immediate maintenance cycle for specific device');
    console.log('  💡 help                - Show this help message');
    console.log('  🚪 exit                - Gracefully shutdown simulator');
    console.log('\n📋 Available Device IDs: laptop001, laptop002, laptop003\n');
    
  } else if (command === 'exit') {
    console.log('👋 Shutting down simulator...');
    readline.close();
    shutdown();
    
  } else {
    console.log('❓ Unknown command. Type "help" for available commands.');
  }
});

// Initial startup message
setTimeout(() => {
  console.log('\n🚀 Simulators are now running...');
  console.log('📡 Generating realistic cybersecurity telemetry every 5 seconds');
  console.log('🔍 Watch how devices improve over time through patches, maintenance, and learning!');
  console.log('📊 Type "status" for quick overview or "detailed" for comprehensive analysis\n');
}, 1000);