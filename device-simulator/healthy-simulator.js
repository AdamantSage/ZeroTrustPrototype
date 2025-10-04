/**
 * Healthy Device Behavior Simulator
 * Generates telemetry representing optimal device behavior to improve trust scores
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
  'Computer-Lab-1': { lat: -26.6875, lng: 27.0935, subnet: '10.1.5' },
  'Student-Center': { lat: -26.6878, lng: 27.0938, subnet: '10.1.7' }
};

/* === HEALTHY DEVICE PROFILES === */
const HEALTHY_PROFILES = {
  laptop001: {
    id: 'laptop001',
    type: 'ENTERPRISE_MANAGED',
    expectedVersion: '1.2.0',
    expectedPatchStatus: 'Up-to-date',
    maxCpuUsage: 85,
    maxMemoryUsage: 90,
    maxNetworkTraffic: 1000,
    preferredLocations: ['Library-Floor1', 'Library-Floor2', 'Computer-Lab-1'],
    workingHours: { start: 8, end: 17 }
  },
  laptop002: {
    id: 'laptop002',
    type: 'BYOD_STUDENT',
    expectedVersion: '1.3.5',
    expectedPatchStatus: 'Up-to-date',
    maxCpuUsage: 80,
    maxMemoryUsage: 85,
    maxNetworkTraffic: 800,
    preferredLocations: ['Student-Center', 'Library-Floor2'],
    workingHours: { start: 10, end: 22 }
  },
  laptop003: {
    id: 'laptop003',
    type: 'LEGACY_SYSTEM',
    expectedVersion: '1.0.3',
    expectedPatchStatus: 'Up-to-date',
    maxCpuUsage: 75,
    maxMemoryUsage: 80,
    maxNetworkTraffic: 600,
    preferredLocations: ['Computer-Lab-1', 'Library-Floor1'],
    workingHours: { start: 6, end: 14 }
  }
};

/* === HEALTHY DEVICE SIMULATOR CLASS === */
class HealthyDeviceSimulator {
  constructor({ id, profile, campusLocations }) {
    this.id = id;
    this.profile = profile;
    this.campusLocations = campusLocations;
    
    this.client = null;
    this.running = false;
    
    // Operational state
    this.currentLocation = profile.preferredLocations[0]; // Start at primary location
    this.lastLocationChange = Date.now();
    this.sessionStart = Date.now();
    this.consecutiveHealthyReports = 0;
    
    console.log(`[${this.id}] 🟢 Initialized Healthy ${this.profile.type} Simulator`);
  }
  
  /* === HEALTHY TELEMETRY GENERATION === */
  generateHealthyTelemetry() {
    const hour = new Date().getHours();
    const workHours = this.profile.workingHours;
    const limits = this.profile.limits;
    
    // Update location occasionally (stable behavior)
    this.updateStableLocation();
    
    const location = this.currentLocation;
    const coords = this.campusLocations[location] || null;
    
    // Generate optimal resource usage
    const resources = this.generateOptimalResourceUsage();
    
    // Build healthy telemetry
    const telemetryData = {
      deviceId: this.id,
      
      // Identity - always valid for healthy devices
      certificateValid: true,
      
      // Firmware - always compliant
      patchStatus: this.profile.expectedPatchStatus,
      firmwareVersion: this.profile.expectedVersion,
      
      // Location
      ipAddress: this.generateIPAddress(location),
      location,
      coordinates: coords,
      
      // Resource usage - well within limits
      cpuUsage: resources.cpuUsage,
      memoryUsage: resources.memoryUsage,
      networkTrafficVolume: resources.networkTrafficVolume,
      
      // Security indicators - all positive
      anomalyScore: this.generateLowAnomalyScore(),
      malwareSignatureDetected: false,
      
      // Session info
      sessionDuration: Math.floor((Date.now() - this.sessionStart) / 1000),
      suspiciousActivityScore: 0,
      consecutiveAnomalies: 0,
      
      // Device profile
      deviceProfile: this.profile.type,
      timestamp: new Date().toISOString(),
      
      // Healthy behavior indicator
      healthyBehaviorIndicator: true,
      consecutiveHealthyReports: this.consecutiveHealthyReports
    };
    
    this.consecutiveHealthyReports++;
    
    return telemetryData;
  }
  
  generateOptimalResourceUsage() {
    const hour = new Date().getHours();
    const workHours = this.profile.workingHours;
    
    // Base load - conservative during working hours
    let baseLoad = 0.3;
    if (hour >= workHours.start && hour <= workHours.end) {
      baseLoad = 0.45; // Moderate during work hours
    }
    
    // Device type adjustments - all efficient
    if (this.profile.type === 'ENTERPRISE_MANAGED') {
      baseLoad *= 0.7; // Very efficient
    } else if (this.profile.type === 'LEGACY_SYSTEM') {
      baseLoad *= 0.85; // Reasonably efficient
    }
    
    return {
      cpuUsage: parseFloat(Math.max(10, Math.min(70, baseLoad * this.profile.maxCpuUsage + (Math.random() - 0.5) * 10)).toFixed(2)),
      memoryUsage: parseFloat(Math.max(15, Math.min(70, baseLoad * this.profile.maxMemoryUsage + (Math.random() - 0.5) * 8)).toFixed(2)),
      networkTrafficVolume: parseFloat(Math.max(50, baseLoad * this.profile.maxNetworkTraffic * 0.6 + Math.random() * 100).toFixed(2))
    };
  }
  
  generateLowAnomalyScore() {
    // Very low anomaly score with slight random variation
    return parseFloat((0.01 + Math.random() * 0.04).toFixed(3)); // 0.01 to 0.05
  }
  
  updateStableLocation() {
    const now = Date.now();
    const hour = new Date().getHours();
    const workHours = this.profile.workingHours;
    
    // Only change location occasionally (every 2-4 hours)
    const timeSinceLastChange = now - this.lastLocationChange;
    const changeInterval = (2 + Math.random() * 2) * 3600000; // 2-4 hours
    
    if (timeSinceLastChange > changeInterval) {
      // During work hours - stay on campus
      if (hour >= workHours.start && hour <= workHours.end) {
        const newLocation = this.profile.preferredLocations[
          Math.floor(Math.random() * this.profile.preferredLocations.length)
        ];
        
        if (newLocation !== this.currentLocation) {
          console.log(`[${this.id}] 📍 Stable location change: ${this.currentLocation} -> ${newLocation}`);
          this.currentLocation = newLocation;
          this.lastLocationChange = now;
        }
      }
    }
  }
  
  generateIPAddress(location) {
    const loc = this.campusLocations[location];
    if (!loc) return `10.1.0.${Math.floor(Math.random() * 254) + 1}`;
    return `${loc.subnet}.${Math.floor(Math.random() * 254) + 1}`;
  }
  
  attachClient(client) {
    this.client = client;
  }
  
  getStatus() {
    return {
      deviceId: this.id,
      type: this.profile.type,
      currentLocation: this.currentLocation,
      consecutiveHealthyReports: this.consecutiveHealthyReports,
      sessionDuration: Math.floor((Date.now() - this.sessionStart) / 1000),
      status: 'HEALTHY'
    };
  }
}

/* === CREATE AND RUN SIMULATORS === */
const simulators = Object.keys(DEVICE_CONNECTIONS).map(deviceId => {
  const profile = HEALTHY_PROFILES[deviceId];
  if (!profile) {
    console.error(`❌ No profile found for device ${deviceId}`);
    return null;
  }
  
  const sim = new HealthyDeviceSimulator({
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
    
    const telemetryData = sim.generateHealthyTelemetry();
    const message = new Message(JSON.stringify(telemetryData));
    
    // Add message properties
    try {
      message.properties.add('priority', 'normal');
      message.properties.add('behavior-type', 'healthy');
    } catch (e) {
      // Ignore property errors
    }
    
    sim.client.sendEvent(message, (err) => {
      if (err) {
        console.error(`[${sim.id}] ❌ Send error:`, err.message);
      } else {
        const coords = telemetryData.coordinates ? 
          `${telemetryData.coordinates.lat.toFixed(4)}, ${telemetryData.coordinates.lng.toFixed(4)}` : 'N/A';
        
        console.log(`[${sim.id}] 🟢 HEALTHY | ${telemetryData.location} (${coords}) | ` +
                   `Anomaly: ${telemetryData.anomalyScore} | CPU: ${telemetryData.cpuUsage}% | ` +
                   `Streak: ${telemetryData.consecutiveHealthyReports}`);
      }
    });
  });
};

// Start telemetry loop
const intervalHandle = setInterval(sendTelemetry, TELEMETRY_INTERVAL);

/* === GRACEFUL SHUTDOWN === */
function shutdown() {
  console.log('\n🔄 Shutting down healthy device simulator...');
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
    console.log('✅ Healthy simulator shutdown complete');
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

console.log('\n🟢 Healthy Device Behavior Simulator Started');
console.log('='.repeat(70));
console.log('📊 This simulator generates optimal telemetry to improve trust scores');
console.log('');
console.log('✅ All devices exhibit:');
console.log('  • Valid certificates and identity');
console.log('  • Up-to-date firmware and patches');
console.log('  • Optimal resource usage');
console.log('  • Stable location patterns');
console.log('  • Zero anomalies or malware');
console.log('  • Full policy compliance');
console.log('');
console.log('💡 Commands: status | help | exit');
console.log('='.repeat(70));

readline.on('line', (line) => {
  const command = line.trim().toLowerCase();
  
  if (command === 'status') {
    console.log('\n📊 Healthy Device Status:');
    console.log('-'.repeat(50));
    simulators.forEach(sim => {
      const status = sim.getStatus();
      console.log(`[${status.deviceId}] 🟢 ${status.type} | ` +
                 `Location: ${status.currentLocation} | ` +
                 `Healthy Reports: ${status.consecutiveHealthyReports} | ` +
                 `Session: ${Math.floor(status.sessionDuration / 60)}m`);
    });
    console.log('');
    
  } else if (command === 'help') {
    console.log('\n💡 Available Commands:');
    console.log('  📊 status  - Show device status overview');
    console.log('  💡 help    - Show this help message');
    console.log('  🚪 exit    - Gracefully shutdown simulator\n');
    
  } else if (command === 'exit') {
    console.log('👋 Shutting down healthy simulator...');
    readline.close();
    shutdown();
    
  } else {
    console.log('❓ Unknown command. Type "help" for available commands.');
  }
});

// Initial startup message
setTimeout(() => {
  console.log('\n🚀 Healthy simulators are now running...');
  console.log('📡 Generating optimal telemetry every 5 seconds');
  console.log('📈 Watch trust scores improve over time!\n');
}, 1000);

//preferred, check location