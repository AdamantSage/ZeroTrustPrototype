/**
 * Simulator for Azure IoT device messages.
 * Replace <YOUR_CONNECTION_STRING> with your IoT Hub device connection string.
 */
const { Client, Message } = require('azure-iot-device');
const Protocol = require('azure-iot-device-mqtt').Mqtt;

const connectionString = 'HostName=myZeroTrustHub.azure-devices.net;DeviceId=device001;SharedAccessKey=0qHVAXIqxqRBTgJLBvjJMDQNzlLRFjikNpGCwJmgfP0=';
const client = Client.fromConnectionString(connectionString, Protocol);

function sendMessage() {
  const message = {
    deviceId: 'device001',
    firmwareVersion: '1.0.0',
    temperature: (20 + Math.random() * 10).toFixed(2),
    timestamp: new Date().toISOString()
  };
  client.sendEvent(new Message(JSON.stringify(message)), err => {
    if (err) console.error('Send error:', err.toString());
    else console.log('Message sent:', message);
  });
}

client.open(err => {
  if (err) return console.error('Could not connect:', err.toString());
  console.log('Client connected');
  setInterval(sendMessage, 5000);
});
