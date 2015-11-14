arduino wifi chat
=================
We want to turn a motor forwards or backwards depending on the message sent to the Arduino.
The motor has been set up already but to make testing easier I am just blinking two LEDs, one for each direction.

I want to talk to the wifi shield from my laptop.
I want to send a message to it from a phone eventually but for know I'm using nc (netcat) to test it.

Check out the [WiFi](https://www.arduino.cc/en/Reference/WiFi) docs. They are nice and have some good examples. Check out the ones inside the pages for the functions and classes too, like [WiFiServerWrite](https://www.arduino.cc/en/Reference/WiFiServerWrite).

## nc
Check out the manual (`man nc` or `man netcat`).
In a terminal this is how you can send a packet with netcat

    nc [ip] [port]
Use the ip of the arduino wifi shield an the port it is using.
For example

    nc 192.168.1.129 2390
Once it is running, type something and press enter.
It will send the text to the shield.
To listen, use `-l`

    nc -l 2390
This will listen for packets coming to your computer.
When you are connected to a device it will get messages from it even without using `-l`.
You can't use `-l` with some of the other options. See the manual:

    -l      Used to specify that nc should listen for an incoming connection
                 rather than initiate a connection to a remote host.  It is an
                 error to use this option in conjunction with the -p, -s, or -z
                 options.  Additionally, any timeouts specified with the -w option
                 are ignored.
Note the `-u` option

    -u      Use UDP instead of the default option of TCP.

My professor recommended udp so I may use this option later.

## talking
    [14:22] <Xeek> markson: looks like tcp, smells like tcp
    [1]
The part of the wifi library that use the client.xyz and server.xyz appear to use tcp.

Using an example chat program and adding blinking code we can make a quick demo to control the light
```
/*
 Chat  Server

 A simple server that distributes any incoming messages to all
 connected clients.  To use telnet to  your device's IP address and type.
 You can see the client's input in the serial monitor as well.

 This example is written for a network using WPA encryption. For
 WEP or WPA, change the Wifi.begin() call accordingly.


 Circuit:
 * WiFi shield attached

 created 18 Dec 2009
 by David A. Mellis
 modified 31 May 2012
 by Tom Igoe

 */

#include <SPI.h>
#include <WiFi.h>
#define OPEN 8
#define CLOSE 9

char ssid[] = "Cisco25707";  //  your network SSID (name)
char pass[] = "password";    // your network password
int status = WL_IDLE_STATUS;

WiFiServer server(2390);

boolean alreadyConnected = false; // whether or not the client was connected previously
void
blink(int pin)
{
  digitalWrite(pin, HIGH);
  delay(1000);
  digitalWrite(pin, LOW);
  delay(1000);
}
void setup() {
    pinMode(OPEN, OUTPUT);
  pinMode(CLOSE, OUTPUT);
  //Initialize serial and wait for port to open:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }

  // check for the presence of the shield:
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    // don't continue:
    while(true);
  }

  // attempt to connect to Wifi network:
  while ( status != WL_CONNECTED) {
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(10000);
  }
  // start the server:
  server.begin();
  // you're connected now, so print out the status:
  printWifiStatus();
 }


void loop() {
  // wait for a new client:
  WiFiClient client = server.available();


  // when the client sends the first byte, say hello:
  if (client) {
    if (!alreadyConnected) {
      // clead out the input buffer:
      client.flush();
      Serial.println("We have a new client");
      client.println("Hello, client!");
      alreadyConnected = true;
    }

    if (client.available() > 0) {
      // read the bytes incoming from the client:
      char thisChar = client.read();
         if (thisChar == 'l') /* lock */
      blink(CLOSE);
    if (thisChar == 'u') /* unlock */
      blink(OPEN);
      // echo the bytes back to the client:
      server.write(thisChar);
      // echo the bytes to the server as well:
      Serial.write(thisChar);
    }
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
```
Listen using nc (`nc 192.168.1.129 2930` for me).
Type `l` to blink pin 9 and `u` to blink 8.
***

[1]: #arduino on irc.freenode.net:

    [14:10] <markson> Hi guys. Does the Arduino wifi class use tcp by default? in the examples when it just says server.xyz and client.xyz like https://www.arduino.cc/en/Tutorial/WiFiChatServer
    ...
    [14:22] <Xeek> markson: looks like tcp, smells like tcp
    [14:20] <Xeek> markson: I could look
    [14:21] == sputnikchen [~Sputnik@178-191-129-44.adsl.highway.telekom.at] has joined #arduino
    [14:22] <Xeek> markson: looks like tcp, smells like tcp
    [14:22] <Xeek> taste it?
    [14:22] <deshipu> if it smells like chicken, you are holding the soldering iron wrong
    [14:23] <sputnikchen> Hello, can i use an arduino with USB to program an arduiono without USB?
    [14:23] <Xeek> yeah... soldered chicken should smell like it was grilled
