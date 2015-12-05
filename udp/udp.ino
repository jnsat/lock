/*
 Arduino Wireless Lock by Joshua And Marcus
 
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
#include <Servo.h>
#include <AES.h>

enum {
  LOCK = 100,
  UNLOCK = 0,
  MOTOR = 9,
  PAUTH = 3,
  KEYBITS = 128,
  MSGBLOCKS = 1
};

long randnum;
int status = WL_IDLE_STATUS;
//char ssid[] = "Cisco25707";
//char pass[] = "password";
char ssid[] = "statewide";
char pass[] = "st88wide";
unsigned int localPort = 2390;

char packetBuffer[255]; //buffer to hold incoming packet
char  ReplyBuffer[] = "acknowledged"; // a string to send back
Servo servo;
WiFiUDP Udp;
AES aes;
IPAddress IP(192, 168, 1, 129);

byte key[] =
{
  0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
};

byte plain[] =
{
  // 0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
  0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
  0xE0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
  0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
};
/* laugh at me */
byte my_iv[] =
{
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
};

byte cipher [4 * N_BLOCK];
byte check [4 * N_BLOCK];
long t0, t1; /* something for aes */
byte succ;
byte iv [N_BLOCK]; /* for interal lib */
void
setkey(int bits, int blocks)
{
  //long t0 = micros () ;
  t0 = micros();
  succ = aes.set_key (key, bits) ;
  long t1 = micros() - t0 ;
  Serial.print ("set_key ") ; Serial.print (bits) ; Serial.print (" ->") ; Serial.print ((int) succ) ;
  Serial.print (" took ") ; Serial.print (t1) ; Serial.println ("us") ;
}

void
encrypt(int bits, int blocks)
{
  t0 = micros () ;
  if (blocks == 1)
    succ = aes.encrypt (plain, cipher) ;
  else
  {
    for (byte i = 0 ; i < 16 ; i++)
      iv[i] = my_iv[i];
    succ = aes.cbc_encrypt (plain, cipher, blocks, iv) ;
  }
  t1 = micros () - t0 ;
  Serial.print ("encrypt ") ; Serial.print ((int) succ) ;
  Serial.print (" took ") ; Serial.print (t1) ; Serial.println ("us") ;
}

void
decrypt(int bits, int blocks)
{
  t0 = micros () ;
  if (blocks == 1)
    succ = aes.decrypt (cipher, plain) ;
  else
  {
    for (byte i = 0 ; i < 16 ; i++)
      iv[i] = my_iv[i];
    succ = aes.cbc_decrypt (cipher, check, blocks, iv) ;
  }
  t1 = micros () - t0 ;
  Serial.print ("decrypt ") ; Serial.print ((int) succ) ;
  Serial.print (" took ") ; Serial.print (t1) ; Serial.println ("us") ;

  for (byte ph = 0 ; ph < (blocks == 1 ? 3 : 4) ; ph++)
  {
    for (byte i = 0 ; i < (ph < 3 ? blocks*N_BLOCK : N_BLOCK) ; i++)
    {
      byte val = ph == 0 ? plain[i] : ph == 1 ? cipher[i] : ph == 2 ? check[i] : iv[i];
      Serial.print (val >> 4, HEX) ; Serial.print (val & 15, HEX) ; Serial.print (" ") ;
    }
    Serial.println () ;
  }
}

void setup() {
  pinMode(PAUTH, INPUT);
  servo.attach(MOTOR);
  randomSeed(analogRead(0)); /* seed with analog pin noise */
  setkey(256, 1); /* aes bits */
 
  Serial.begin(9600);
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
  WiFi.config(IP);
  Udp.begin(localPort);
  Serial.println("Finished setup");
}

void loop() {
  /* test auth setup button. could be used to send aes key accross network once. */
  //Serial.println("PAUTH");
  //Serial.println(digitalRead(PAUTH));
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
    //decrypt(packetBuffer);
    char c = packetBuffer[0];
    plain[0] = c; /* first char */
    //decrypt(KEYBITS, MSGBLOCKS);
    if (c == 'l') { /* lock */
      servo.write(LOCK);
      Serial.println("lock");
    }
    else if (c == 'u') { /* unlock */
      servo.write(UNLOCK);
      Serial.println("unlock");
    }
    else if (c == 'r') {
      randnum = random(999999);
      Serial.println(randnum);
    }
    else if (c == 'a') { /* auth prep */
      /* send iv like a challenge, no need to encrypt.
      should be rand and uniq for real world to prevent replay */
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      //geniv(); /* should be random each time */
      Udp.write((char *)my_iv);
      Udp.endPacket();
    } else {
      Serial.print("err: did not understand command:->");
      Serial.print(packetBuffer);
      Serial.print("<-\n");
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
