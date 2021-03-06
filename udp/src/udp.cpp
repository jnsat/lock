/*
 Run this on an Arduino with a WiFi Shield.
 in a terminal run
nc -u [shield's ip] 2390
  e.g.
nc -u 192.168.1.129 2390
 send l to sim. lock and u to sim. unlock. a light will blink. 

 based on:
 WiFi UDP Send and Receive String
 This sketch wait an UDP packet on localPort using a WiFi shield.
 When a packet is received an Acknowledge packet is sent to the client on port remotePort

 created 30 December 2012
 by dlf (Metodo2 srl)
 blinking for testing added by Joshua
*/
#include <SPI.h>
#include <WiFi.h>
#include <WiFiUdp.h>
#define OPEN 8
#define CLOSE 9
#define SHORT 100
long randnum;
int status = WL_IDLE_STATUS;
char ssid[] = "Cisco25707";  //  your network SSID (name)
//char ssid[] = "Cisco60921";  //  your network SSID (name)
char pass[] = "password";    // your network password
int keyIndex = 0;            // your network key Index number (needed only for WEP)

unsigned int localPort = 2390;      // local port to listen on

char packetBuffer[255]; //buffer to hold incoming packet
char  ReplyBuffer[] = "acknowledged";       // a string to send back

WiFiUDP Udp;

void
blink(int pin)
{
  digitalWrite(pin, HIGH);
  delay(SHORT);
  digitalWrite(pin, LOW);
  delay(SHORT);
}

void setup() {
  pinMode(OPEN, OUTPUT);
  pinMode(CLOSE, OUTPUT);
  //Initialize serial and wait for port to open:
  Serial.begin(9600);
  randomSeed(analogRead(0)); /* seed with analog pin noise */
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }

  // check for the presence of the shield:
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    // don't continue:
    while (true);
  }

  String fv = WiFi.firmwareVersion();
  if (fv != "1.1.0") {
    Serial.println("Please upgrade the firmware");
  }

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
    status = WiFi.begin(ssid, pass);
    // wait 10 seconds for connection:
    delay(10000);
  }
  Serial.println("Connected to wifi");
  printWifiStatus();

  Serial.println("\nStarting connection to server...");
  // if you get a connection, report back via serial:
  Udp.begin(localPort);
}

void loop() {
  // if there's data available, read a packet
  int packetSize = Udp.parsePacket();
  if (packetSize) {
    Serial.print("Received packet of size ");
    Serial.println(packetSize);
    Serial.print("From ");
    IPAddress remoteIp = Udp.remoteIP();
    Serial.print(remoteIp);
    Serial.print(", port ");
    Serial.println(Udp.remotePort());

    // read the packet into packetBufffer
    int len = Udp.read(packetBuffer, 255);
    if (len > 0) packetBuffer[len] = 0;
    Serial.println("Contents:");
    Serial.println(packetBuffer);
    char c = packetBuffer[0];
    if (c == 'l') /* lock */
      blink(CLOSE);
    else if (c == 'u') /* unlock */
      blink(OPEN);
    else if (c == 'r') {
      randnum = random(999999);
      Serial.println(randnum);
    } else {
      Serial.print("err: did not understand command: ");
      Serial.print(packetBuffer);
      Serial.print(".\n");
    }
    // send a reply, to the IP address and port that sent us the packet we received
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();
    Serial.print(Udp.remoteIP());
    Serial.print(" ");
    Serial.print(Udp.remotePort());
  }
}

void printWifiStatus() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}
