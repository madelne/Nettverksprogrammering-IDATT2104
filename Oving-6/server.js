const net = require("net");
const crypto = require("crypto");

const clients = new Set();

const wsServer = net.createServer((connection) => {
  console.log("Client connected");

  connection.on("data", (data) => {
    if (data.toString().includes("Sec-WebSocket-Key")) {
      performHandshake(connection, data.toString());
    } else if (connection.isWebSocket) {
      const message = decodeMessage(data);
      console.log("Received message:", message);
      broadcastMessage(message);
    }
  });

  connection.on("end", () => {
    console.log("Client disconnected");
    clients.delete(connection);
  });
});

// Håndterer WebSocket-handshake
function performHandshake(connection, headers) {
  const keyMatch = headers.match(/Sec-WebSocket-Key: (.+)/);
  if (!keyMatch) return;
  const key = keyMatch[1].trim();
  const acceptKey = generateAcceptKey(key);

  const response = [
    "HTTP/1.1 101 Switching Protocols",
    "Upgrade: websocket",
    "Connection: Upgrade",
    `Sec-WebSocket-Accept: ${acceptKey}`,
    "\r\n",
  ].join("\r\n");

  connection.write(response);
  connection.isWebSocket = true;
  clients.add(connection);
}

// Genererer WebSocket-accept key
function generateAcceptKey(key) {
  return crypto
    .createHash("sha1")
    .update(key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
    .digest("base64");
}

// Dekoder WebSocket-meldinger
function decodeMessage(data) {
  if (data.length < 6) return "";
  const length = data[1] & 127;
  let maskStart = 2;
  if (length === 126) maskStart = 4;
  else if (length === 127) maskStart = 10;

  const mask = data.slice(maskStart, maskStart + 4);
  const messageStart = maskStart + 4;
  const message = data.slice(messageStart, messageStart + length);
  let decoded = "";
  for (let i = 0; i < message.length; i++) {
    decoded += String.fromCharCode(message[i] ^ mask[i % 4]);
  }
  return decoded;
}

// Koder meldinger før sending
function encodeMessage(message) {
  const messageBuffer = Buffer.from(message);
  const length = messageBuffer.length;
  let buffer;

  if (length <= 125) {
    buffer = Buffer.alloc(2 + length);
    buffer[1] = length;
  } else if (length <= 65535) {
    buffer = Buffer.alloc(4 + length);
    buffer[1] = 126;
    buffer.writeUInt16BE(length, 2);
  } else {
    buffer = Buffer.alloc(10 + length);
    buffer[1] = 127;
    buffer.writeBigUInt64BE(BigInt(length), 2);
  }

  buffer[0] = 0x81;
  messageBuffer.copy(buffer, buffer.length - length);

  return buffer;
}

// Sender meldinger til alle tilkoblede klienter
function broadcastMessage(message) {
  const encodedMessage = encodeMessage(message);
  for (const client of clients) {
    if (client.isWebSocket) {
      client.write(encodedMessage);
    }
  }
}

wsServer.listen(3001, () => {
  console.log("WebSocket server listening on port 3001");
});
