/**
 * Enhanced Multi-Laptop Simulator (3 laptops, each with own connection string)
 * Sends telemetry to Azure IoT Hub every 5 seconds.
 * Features: Stable location changes, realistic movement patterns, enhanced intrusion detection
 */
const { Client, Message } = require('azure-iot-device');
const Protocol = require('azure-iot-device-mqtt').Mqtt;

// Map each DeviceId → its full Primary Connection String
const DEVICE_CONNECTIONS = {
  laptop001: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop001;SharedAccessKey=hDhxjBVTDGBXjhNk+Kmo/GW5IZYyK1jWK5WjjtVJS1E=',
  laptop002: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop002;SharedAccessKey=bbqdM7Q+HRY20mo2djn7H6dc/zmu5h+rw8CGBo9iDbU=',
  laptop003: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop003;SharedAccessKey=r6TOvCuGn6pFlyzLxeyYcnwK89MsHW9xyFG+io/5pJU='
};

// Device-specific expected versions (matching your database)
const DEVICE_EXPECTED_VERSIONS = {
  laptop001: '1.2.0',
  laptop002: '1.3.5', 
  laptop003: '1.0.3'
};

// Campus locations with coordinates for mapping
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

// Device behavior profiles for demonstration
const DEVICE_PROFILES = {
  laptop001: {
    type: 'SAFE_DEVICE',
    preferredLocations: ['Library-Floor1', 'Library-Floor2', 'Computer-Lab-1'],
    locationChangeFrequency: 3600000, // 1 hour
    anomalyRate: 0.02,
    complianceRate: 0.95
  },
  laptop002: {
    type: 'SUSPICIOUS_DEVICE',
    preferredLocations: ['Lecture-Hall-A', 'Student-Center', 'Off-Campus-Cafe', 'Off-Campus-Home'],
    locationChangeFrequency: 900000, // 15 minutes (frequent changes)
    anomalyRate: 0.15,
    complianceRate: 0.60
  },
  laptop003: {
    type: 'COMPROMISED_DEVICE',
    preferredLocations: ['Computer-Lab-2', 'Admin-Building', 'Off-Campus-Home'],
    locationChangeFrequency: 600000, // 10 minutes (very frequent)
    anomalyRate: 0.25,
    complianceRate: 0.40
  }
};

// Create device state with enhanced tracking
const devices = Object.keys(DEVICE_CONNECTIONS).map(id => ({
  id,
  sessionStart: Date.now(),
  client: null,
  currentLocation: null,
  lastLocationChange: Date.now(),
  locationHistory: [],
  consecutiveAnomalies: 0,
  suspiciousActivityScore: 0,
  profile: DEVICE_PROFILES[id]
}));

// Enhanced utility functions
function initializeDeviceLocation(device) {
  if (!device.currentLocation) {
    const locations = device.profile.preferredLocations;
    device.currentLocation = locations[Math.floor(Math.random() * locations.length)];
    device.lastLocationChange = Date.now();
  }
}

function shouldChangeLocation(device) {
  const timeSinceLastChange = Date.now() - device.lastLocationChange;
  const baseFrequency = device.profile.locationChangeFrequency;
  
  // Add some randomness (±30%)
  const randomFactor = 0.7 + (Math.random() * 0.6);
  const adjustedFrequency = baseFrequency * randomFactor;
  
  return timeSinceLastChange > adjustedFrequency;
}

function getNewLocation(device) {
  if (!shouldChangeLocation(device)) {
    return device.currentLocation;
  }
  
  const profile = device.profile;
  let newLocation;
  
  // Suspicious devices move more erratically
  if (profile.type === 'SUSPICIOUS_DEVICE' && Math.random() < 0.3) {
    // Sometimes go to random locations
    const allLocations = Object.keys(CAMPUS_LOCATIONS);
    newLocation = allLocations[Math.floor(Math.random() * allLocations.length)];
  } else if (profile.type === 'COMPROMISED_DEVICE' && Math.random() < 0.4) {
    // Compromised devices sometimes try to access admin areas
    const sensitiveLocations = ['Admin-Building', 'Computer-Lab-1', 'Computer-Lab-2'];
    newLocation = sensitiveLocations[Math.floor(Math.random() * sensitiveLocations.length)];
  } else {
    // Normal behavior - stay within preferred locations
    const preferredLocations = profile.preferredLocations.filter(loc => loc !== device.currentLocation);
    newLocation = preferredLocations[Math.floor(Math.random() * preferredLocations.length)] || device.currentLocation;
  }
  
  if (newLocation !== device.currentLocation) {
    // Track location history for anomaly detection
    device.locationHistory.push({
      from: device.currentLocation,
      to: newLocation,
      timestamp: new Date(),
      coordinates: CAMPUS_LOCATIONS[newLocation]
    });
    
    // Keep only last 10 location changes
    if (device.locationHistory.length > 10) {
      device.locationHistory.shift();
    }
    
    device.currentLocation = newLocation;
    device.lastLocationChange = Date.now();
    
    console.log(`[${device.id}] Location changed to: ${newLocation} (${CAMPUS_LOCATIONS[newLocation].lat}, ${CAMPUS_LOCATIONS[newLocation].lng})`);
  }
  
  return newLocation;
}

function generateIPAddress(location) {
  const locationData = CAMPUS_LOCATIONS[location];
  if (!locationData) return '10.1.0.' + (Math.floor(Math.random() * 254) + 1);
  
  const subnet = locationData.subnet;
  return subnet + '.' + (Math.floor(Math.random() * 254) + 1);
}

function randomFirmware(deviceId) {
  const expectedVersion = DEVICE_EXPECTED_VERSIONS[deviceId];
  const profile = DEVICE_PROFILES[deviceId];
  
  // Compliance rate determines version adherence
  if (Math.random() < profile.complianceRate) {
    return expectedVersion;
  }
  
  // Non-compliant versions
  const nonCompliantVersions = {
    laptop001: ['1.0.0', '1.0.1', '1.0.2', '1.0.3', '1.0.4', '1.1.0', '1.1.1'],
    laptop002: ['1.0.0', '1.0.1', '1.2.0', '1.3.0', '1.3.1', '1.3.2', '1.3.3', '1.3.4'],
    laptop003: ['1.0.0', '1.0.1', '1.0.2', '1.0.4', '1.0.5']
  };
  
  const options = nonCompliantVersions[deviceId] || ['1.0.0', '1.0.1', '1.0.2'];
  return options[Math.floor(Math.random() * options.length)];
}

function randomPatchStatus(deviceId) {
  const profile = DEVICE_PROFILES[deviceId];
  
  const expectedPatchStatus = {
    laptop001: 'Up-to-date',
    laptop002: 'Up-to-date',
    laptop003: 'Outdated'
  };
  
  const expected = expectedPatchStatus[deviceId];
  
  // Compliance rate affects patch status
  if (Math.random() < profile.complianceRate) {
    return expected;
  }
  
  return expected === 'Up-to-date' ? 'Outdated' : 'Up-to-date';
}

function randomCertificateValid(deviceId) {
  const profile = DEVICE_PROFILES[deviceId];
  
  // laptop003 doesn't require certificates
  if (deviceId === 'laptop003') {
    return Math.random() > 0.1;
  }
  
  // Compliance affects certificate validity
  return Math.random() < profile.complianceRate;
}

function randomAnomalyScore(device) {
  const profile = device.profile;
  let baseScore = Math.random();
  
  // Adjust based on device profile
  if (profile.type === 'SUSPICIOUS_DEVICE') {
    baseScore += 0.2;
  } else if (profile.type === 'COMPROMISED_DEVICE') {
    baseScore += 0.4;
  }
  
  // Factor in recent location changes (rapid changes = higher anomaly)
  const recentChanges = device.locationHistory.filter(
    change => new Date() - change.timestamp < 3600000 // last hour
  ).length;
  
  if (recentChanges > 3) {
    baseScore += 0.3;
  }
  
  return Math.min(baseScore, 1.0).toFixed(2);
}

function randomMalwareFlag(device) {
  const profile = device.profile;
  
  // Higher malware detection for suspicious/compromised devices
  let malwareRate = 0.01;
  if (profile.type === 'SUSPICIOUS_DEVICE') malwareRate = 0.05;
  if (profile.type === 'COMPROMISED_DEVICE') malwareRate = 0.08;
  
  const malwareDetected = Math.random() < malwareRate;
  
  if (malwareDetected) {
    device.consecutiveAnomalies++;
    device.suspiciousActivityScore += 10;
  } else if (device.consecutiveAnomalies > 0) {
    device.consecutiveAnomalies--;
    device.suspiciousActivityScore = Math.max(0, device.suspiciousActivityScore - 1);
  }
  
  return malwareDetected;
}

function generateResourceUsage(device) {
  const deviceId = device.id;
  const profile = device.profile;
  
  // Device-specific resource usage based on database limits
  const limits = {
    laptop001: { cpu: 85, memory: 90, network: 1000 },
    laptop002: { cpu: 80, memory: 85, network: 800 },
    laptop003: { cpu: 75, memory: 80, network: 600 }
  };
  
  const deviceLimits = limits[deviceId];
  
  // Suspicious/compromised devices more likely to exceed limits
  let exceedChance = 0.1;
  if (profile.type === 'SUSPICIOUS_DEVICE') exceedChance = 0.25;
  if (profile.type === 'COMPROMISED_DEVICE') exceedChance = 0.35;
  
  // Factor in suspicious activity score
  exceedChance += (device.suspiciousActivityScore / 1000);
  
  return {
    cpuUsage: Math.random() < exceedChance 
      ? +(Math.random() * (100 - deviceLimits.cpu) + deviceLimits.cpu).toFixed(2)
      : +(Math.random() * deviceLimits.cpu).toFixed(2),
    
    memoryUsage: Math.random() < exceedChance
      ? +(Math.random() * (100 - deviceLimits.memory) + deviceLimits.memory).toFixed(2)
      : +(Math.random() * deviceLimits.memory).toFixed(2),
    
    networkTrafficVolume: Math.random() < exceedChance
      ? +(Math.random() * (deviceLimits.network * 0.5) + deviceLimits.network).toFixed(2)
      : +(Math.random() * deviceLimits.network).toFixed(2)
  };
}

function maybeResetSession(device, location, certValid) {
  if (!certValid || location.includes('Off-Campus')) {
    device.sessionStart = Date.now();
    console.log(`[${device.id}] Session reset due to`, !certValid ? 'invalid cert' : 'off-campus move');
  }
}

function getSessionDuration(device) {
  return Math.floor((Date.now() - device.sessionStart) / 1000);
}

function getTelemetryFor(device) {
  // Initialize location if needed
  initializeDeviceLocation(device);
  
  const location = getNewLocation(device);
  const firmwareVersion = randomFirmware(device.id);
  const patchStatus = randomPatchStatus(device.id);
  const certificateValid = randomCertificateValid(device.id);
  const ipAddress = generateIPAddress(location);
  const resourceUsage = generateResourceUsage(device);
  const locationCoords = CAMPUS_LOCATIONS[location];
  
  maybeResetSession(device, location, certificateValid);

  return {
    deviceId: device.id,
    certificateValid,
    patchStatus,
    firmwareVersion,
    ipAddress,
    location,
    coordinates: locationCoords,
    locationHistory: device.locationHistory.slice(-5), // Last 5 location changes
    ...resourceUsage,
    anomalyScore: randomAnomalyScore(device),
    malwareSignatureDetected: randomMalwareFlag(device),
    sessionDuration: getSessionDuration(device),
    suspiciousActivityScore: device.suspiciousActivityScore,
    consecutiveAnomalies: device.consecutiveAnomalies,
    deviceProfile: device.profile.type,
    timestamp: new Date().toISOString()
  };
}

// Open connection per device
devices.forEach(device => {
  const cs = DEVICE_CONNECTIONS[device.id];
  const client = Client.fromConnectionString(cs, Protocol);

  client.open(err => {
    if (err) {
      console.error(`[${device.id}] Connection error:`, err);
    } else {
      console.log(`[${device.id}] Connected to Azure IoT Hub (Profile: ${device.profile.type})`);
    }
  });

  device.client = client;
});

// Enhanced telemetry sending with intrusion detection alerts
setInterval(() => {
  devices.forEach(device => {
    if (!device.client) return;
    const payload = getTelemetryFor(device);
    const msg = new Message(JSON.stringify(payload));
    
    // Add intrusion detection metadata
    if (payload.suspiciousActivityScore > 50) {
      msg.properties.add('priority', 'high');
      msg.properties.add('alert', 'suspicious-activity');
    }
    
    if (payload.consecutiveAnomalies > 3) {
      msg.properties.add('priority', 'critical');
      msg.properties.add('alert', 'potential-compromise');
    }

    device.client.sendEvent(msg, err => {
      if (err) {
        console.error(`[${device.id}] Send error:`, err);
      } else {
        const alertInfo = payload.suspiciousActivityScore > 50 ? ' [SUSPICIOUS]' : 
                         payload.consecutiveAnomalies > 3 ? ' [CRITICAL]' : '';
        console.log(`[${device.id}] Telemetry sent: ${payload.location} (${payload.coordinates.lat}, ${payload.coordinates.lng})${alertInfo}`);
      }
    });
  });
}, 5000);

// Graceful shutdown
process.on('SIGINT', () => {
  console.log('Shutting down simulator...');
  devices.forEach(device => {
    if (device.client) {
      device.client.close(() => {
        console.log(`[${device.id}] Connection closed`);
      });
    }
  });
  process.exit(0);
});