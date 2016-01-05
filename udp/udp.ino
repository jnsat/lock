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

 thank Robert Bares http://forum.arduino.cc/index.php?topic=38107.0
 for hex printing code
*/
#include <SPI.h>
#include <WiFi.h>
#include <WiFiUdp.h>
#include <Servo.h>
#include <AES.h>

enum {
  /* degress for servo on door lock */
  LOCK = 100,
  UNLOCK = 0,

  /* servo port */
  MOTOR = 9,
  /* auth button pin */
  PAUTH = 3,

  /* aes */
  KEYBITS = 128,
  MSGBLOCKS = 1,
  IV_LEN = 16,
  
  BYTE_MAX = 255,

  LOCALPORT = 2390,

  CHARS_PER_BYTE = 2
};

char packetBuffer[IV_LEN + KEYBITS]; //buffer to hold incoming packet
char  ReplyBuffer[] = "ack\n"; // a string to send back
Servo servo;
WiFiUDP Udp;
AES aes;


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

void p(byte X) { /* http://stackoverflow.com/questions/19127945/how-to-serial-print-full-hexadecimal-bytes */
   if (X < 16) {Serial.print("0");}
   Serial.print(X, HEX);
   Serial.print(" ");
}

void p_udp(byte X) { /* http://stackoverflow.com/questions/19127945/how-to-serial-print-full-hexadecimal-bytes */
   if (X < 16) {Udp.print("0");}
   Udp.print(X, HEX);
   //Udp.print(" ");
}

void
print_iv_udp(unsigned char *iv)
{
  for (int i = 0; i < IV_LEN; i++)
    p_udp(iv[i]);
}

void print_iv(unsigned char *iv) {
  for (int i = 0; i < IV_LEN; i++)
    p(iv[i]);
}

void
hex2char(byte *bytes, char *out, int len, int spaces)
{
    int char_per_byte = CHARS_PER_BYTE + spaces; /* high bit, low bit, optional space 0x[AB ] */
    for (int i = 0; i < len; i++) { /* convert hex to char keeping zeros */
      /* Robert Bares http://forum.arduino.cc/index.php?topic=38107.0 */
      int first = (bytes[i] >> 4) & 0x0f;
      int second = bytes[i] & 0x0f;
      sprintf(&out[i * char_per_byte], spaces ? "%01X%01X " : "%01X%01X", first, second);
    }
}

void
debug_iv()
{
      Serial.print("my_iv: [");
      print_iv(my_iv);
      Serial.print("]\n");
}

void
gen_iv(byte iv[], int len) {
  for (int i = 0; i < IV_LEN; i++)
    iv[i] = random(BYTE_MAX); /* rand byte 0x00 to 0xff */
}

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

void
auth(char c)
{
      debug_iv();
      gen_iv(my_iv, IV_LEN); /* randomize iv */
      debug_iv();
      
      /* send iv like a challenge, no need to encrypt.
      should be rand and uniq for real world to prevent replay */
      Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
      /* write iv all at once, doing each byte one at a time cuts it off after 13 out of 16 */
      int spaces = c == 'A'; /* captial commands mean format pretty (w spaces) */
      int char_per_byte = CHARS_PER_BYTE + spaces;
      char buf[IV_LEN * char_per_byte];
      hex2char(my_iv, buf, IV_LEN, spaces);
      Udp.write(buf);
      Udp.endPacket();
}

void setup() {
  int status = WL_IDLE_STATUS;
  /*char ssid[] = "Cisco25707";
  char pass[] = "password";*/
  char ssid[] = "statewide";
  char pass[] = "st88wide";
  
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
    Serial.println(F("WiFi shield not present"));
    // don't continue:
    while (true);
  }

  String fv = WiFi.firmwareVersion();
  if (fv != "1.1.0") {
    Serial.println(F("Please upgrade the firmware"));
  }

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    Serial.print(F("Attempting to connect to SSID: "));
    Serial.println(ssid);
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
    status = WiFi.begin(ssid, pass);
    // wait 10 seconds for connection:
    delay(10000);
  }
  Serial.println(F("Connected to wifi"));
  printWifiStatus();

  Serial.println(F("\nStarting connection to server..."));
  // if you get a connection, report back via serial:
  //WiFi.config(IP); // not possible to set static ip for udp...
  Udp.begin(LOCALPORT);

  encrypt(128, 4);
  decrypt(128, 4);
  Serial.println(F("Setup done"));
}

void loop() {
  /* test auth setup button. could be used to send aes key accross network once. */
  //Serial.println("PAUTH");
  //Serial.println(digitalRead(PAUTH));
  
  // if there's data available, read a packet
  int packetSize = Udp.parsePacket();
  if (packetSize) {
    Serial.print(F("Received packet of size "));
    Serial.println(packetSize);
    Serial.print(F("From "));
    IPAddress remoteIp = Udp.remoteIP();
    Serial.print(remoteIp);
    Serial.print(F(", port "));
    Serial.println(Udp.remotePort());

    // read the packet into packetBufffer
    int len = Udp.read(packetBuffer, 255);
    if (len > 0) packetBuffer[len] = 0;
    Serial.print(F("Contents: ["));
    Serial.print(packetBuffer);
    Serial.println(F("]"));
    //decrypt(packetBuffer);
    decrypt(KEYBITS, 5); /* what's bits? */
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
    else if (c == 'a' || c == 'A') {
      auth(c);
    } else {
     Serial.printf(F("err: did not understand command:[%s]\n"), packetBuffer);
    }
    // send a reply, to the IP address and port that sent us the packet we received
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(ReplyBuffer);
    Udp.endPacket();
    Serial.print(Udp.remoteIP());
    Serial.print(F(" "));
    Serial.print(Udp.remotePort());
  }
}

void printWifiStatus() {
  // print the SSID of the network you're attached to:
  Serial.print(F("SSID: "));
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print(F("IP Address: "));
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print(F("signal strength (RSSI):"));
  Serial.print(rssi);
  Serial.println(F(" dBm"));
}
