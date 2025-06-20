/**
 * Multi‑Laptop Simulator (3 laptops, each with own connection string)
 * Sends telemetry to Azure IoT Hub every 5 seconds.
 */

const { Client, Message } = require('azure-iot-device');
const Protocol = require('azure-iot-device-mqtt').Mqtt;

//Map each DeviceId → its full Primary Connection String
const DEVICE_CONNECTIONS = {
  laptop001: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop002;SharedAccessKey=bbqdM7Q+HRY20mo2djn7H6dc/zmu5h+rw8CGBo9iDbU=',
  laptop002: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop002;SharedAccessKey=bbqdM7Q+HRY20mo2djn7H6dc/zmu5h+rw8CGBo9iDbU=',
  laptop003: 'HostName=myIoTZeroTrustHub.azure-devices.net;DeviceId=laptop003;SharedAccessKey=r6TOvCuGn6pFlyzLxeyYcnwK89MsHW9xyFG+io/5pJU='
};

//Create device state
const devices = Object.keys(DEVICE_CONNECTIONS).map(id => ({
  id,
  sessionStart: Date.now(),
  client: null
}));

// Utility functions
function randomFirmware() {
  return Math.random() > 0.1 ? '1.0.0' : '0.9.' + (Math.floor(Math.random() * 9) + 1);
}
function randomPatchStatus() {
  return Math.random() > 0.1 ? 'Up-to-date' : 'Outdated';
}
function randomCertificateValid() {
  return Math.random() > 0.05;
}
function pickIP() {
  return Math.random() < 0.7
    ? `10.1.0.${Math.floor(Math.random() * 254) + 1}`
    : `192.168.43.${Math.floor(Math.random() * 254) + 1}`;
}
function pickLocation() {
  return ['Lecture Hall','Library','Lab','Off-Campus'][Math.floor(Math.random() * 4)];
}
function randomAnomalyScore() {
  return +Math.random().toFixed(2);
}
function randomMalwareFlag() {
  return Math.random() < 0.02;
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
  const firmwareVersion = randomFirmware();
  const patchStatus = randomPatchStatus();
  const certificateValid = randomCertificateValid();
  const ipAddress = pickIP();
  const location = pickLocation();
  maybeResetSession(device, location, certificateValid);

  return {
    deviceId: device.id,
    certificateValid,
    patchStatus,
    firmwareVersion,
    ipAddress,
    location,
    cpuUsage: +(Math.random() * 100).toFixed(2),
    memoryUsage: +(Math.random() * 100).toFixed(2),
    networkTrafficVolume: +(Math.random() * 500).toFixed(2),
    anomalyScore: randomAnomalyScore(),
    malwareSignatureDetected: randomMalwareFlag(),
    sessionDuration: getSessionDuration(device),
    timestamp: new Date().toISOString()
  };
}

//Open connection per device
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

//Send telemetry every 5s
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
