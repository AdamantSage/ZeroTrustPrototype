/**
 * Multi-Laptop Simulator (3 laptops, each with own connection string)
 * Sends telemetry to Azure IoT Hub every 5 seconds.
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

// Create device state
const devices = Object.keys(DEVICE_CONNECTIONS).map(id => ({
  id,
  sessionStart: Date.now(),
  client: null
}));

// Utility functions
function randomFirmware(deviceId) {
  const expectedVersion = DEVICE_EXPECTED_VERSIONS[deviceId];
  
  // 70% chance of having the expected (compliant) version
  if (Math.random() < 0.7) {
    return expectedVersion;
  }
  
  // 30% chance of having a non-compliant version
  const nonCompliantVersions = {
    laptop001: ['1.0.0', '1.0.1', '1.0.2', '1.0.3', '1.0.4', '1.1.0', '1.1.1'],
    laptop002: ['1.0.0', '1.0.1', '1.2.0', '1.3.0', '1.3.1', '1.3.2', '1.3.3', '1.3.4'],
    laptop003: ['1.0.0', '1.0.1', '1.0.2', '1.0.4', '1.0.5']
  };
  
  const options = nonCompliantVersions[deviceId] || ['1.0.0', '1.0.1', '1.0.2'];
  return options[Math.floor(Math.random() * options.length)];
}

function randomPatchStatus(deviceId) {
  // Device-specific patch status expectations
  const expectedPatchStatus = {
    laptop001: 'Up-to-date',
    laptop002: 'Up-to-date',
    laptop003: 'Outdated'
  };
  
  const expected = expectedPatchStatus[deviceId];
  
  // 80% chance of having expected patch status
  if (Math.random() < 0.8) {
    return expected;
  }
  
  // 20% chance of having the opposite
  return expected === 'Up-to-date' ? 'Outdated' : 'Up-to-date';
}

function randomCertificateValid(deviceId) {
  // laptop003 doesn't require certificates (certificate_required = 0)
  if (deviceId === 'laptop003') {
    return Math.random() > 0.1; // 90% valid
  }
  
  // laptop001 and laptop002 require certificates
  return Math.random() > 0.05; // 95% valid
}

function pickIP() {
  return Math.random() < 0.7
    ? `10.1.0.${Math.floor(Math.random() * 254) + 1}`
    : `192.168.43.${Math.floor(Math.random() * 254) + 1}`;
}

function pickLocation() {
  return ['Lecture Hall', 'Library', 'Lab', 'Off-Campus'][Math.floor(Math.random() * 4)];
}

function randomAnomalyScore() {
  return +Math.random().toFixed(2);
}

function randomMalwareFlag() {
  return Math.random() < 0.02;
}

function generateResourceUsage(deviceId) {
  // Device-specific resource usage based on database limits
  const limits = {
    laptop001: { cpu: 85, memory: 90, network: 1000 },
    laptop002: { cpu: 80, memory: 85, network: 800 },
    laptop003: { cpu: 75, memory: 80, network: 600 }
  };
  
  const deviceLimits = limits[deviceId];
  
  // Generate usage that sometimes exceeds limits (20% chance)
  const exceedChance = 0.2;
  
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
  if (!certValid || location === 'Off-Campus') {
    device.sessionStart = Date.now();
    console.log(`[${device.id}] Session reset due to`, !certValid ? 'invalid cert' : 'off-campus move');
  }
}

function getSessionDuration(device) {
  return Math.floor((Date.now() - device.sessionStart) / 1000);
}

function getTelemetryFor(device) {
  const firmwareVersion = randomFirmware(device.id);
  const patchStatus = randomPatchStatus(device.id);
  const certificateValid = randomCertificateValid(device.id);
  const ipAddress = pickIP();
  const location = pickLocation();
  const resourceUsage = generateResourceUsage(device.id);
  
  maybeResetSession(device, location, certificateValid);

  return {
    deviceId: device.id,
    certificateValid,
    patchStatus,
    firmwareVersion,
    ipAddress,
    location,
    ...resourceUsage,
    anomalyScore: randomAnomalyScore(),
    malwareSignatureDetected: randomMalwareFlag(),
    sessionDuration: getSessionDuration(device),
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
      console.log(`[${device.id}] Connected to Azure IoT Hub`);
    }
  });

  device.client = client;
});

// Send telemetry every 5s
setInterval(() => {
  devices.forEach(device => {
    if (!device.client) return;
    const payload = getTelemetryFor(device);
    const msg = new Message(JSON.stringify(payload));

    device.client.sendEvent(msg, err => {
      if (err) {
        console.error(`[${device.id}] Send error:`, err);
      } else {
        console.log(`[${device.id}] Telemetry sent:`, payload);
      }
    });
  });
}, 5000);