arduino wifi
============
We tried using an Xbee Zigbee.
They are for mesh communications so they don't do Wifi.
Then we tried to use and Xbee Wifi.
I couldn't get it to initialize so I got an Arduino Wifi shield.
## docs
[ArduinoWiFiShield](https://www.arduino.cc/en/Main/ArduinoWiFiShield)  
[WiFiShieldFirmwareUpgrading](https://www.arduino.cc/en/Hacking/WiFiShieldFirmwareUpgrading)  
[ArduinoWiFiShield](https://www.arduino.cc/en/Guide/ArduinoWiFiShield)  
[WiFi](https://www.arduino.cc/en/Reference/WiFi)  

## firmware
arduino.cc advises upgrading the firmware first.
To upgrade the firmware first download a programmer.
* Windows: [Atmel's flip](http://www.atmel.com/tools/FLIP.aspx)
* Mac: install dfu-programmer using   [macports](https://www.macports.org/install.php#pkg).  
  In Yosemite the installer stopped on the pentultimate step. Just quit it [1] (don't sue). Run:
      sudo port selfupdate
      sudo port install dfu-programmer
      sudo port upgrade outdated
* Other: install `dfu-programmer`

There are firmware installation scripts in `/Applications/Arduino.app/Contents/Java/hardware/arduino/avr/firmwares/wifishield/scripts`

You might save a copy of the right script somewhere easy to get to. Use the one ending in '_mac' if you're on a mac.

Connect the J3 jumper. It is a small rectangular piece of plastic which comes attached to one small metal pole on the shield. Pull it up and put it over both of the pins there.
Then run:

    chmod a+x ./ArduinoWifiShield_upgrade_mac.sh
    ./ArduinoWifiShield_upgrade_mac.sh -a /Applications/Arduino.app/Contents/Java -f shield


## code
Example (based on arduino.cc's)
```
/* code from https://www.arduino.cc/en/Guide/ArduinoWiFiShield
 * https://www.arduino.cc/en/Reference/WiFiLocalIP
 * etc. public domain according to them.
 */
#include <SPI.h>
#include <WiFi.h>

char ssid[] = "ssid";
char pass[] = "password";
int status = WL_IDLE_STATUS; // wifi radio's status
IPAddress ip;

void setup() {1
  Serial.begin(9600);
  Serial.println("Attempting to connect to WPA network...");
  status = WiFi.begin(ssid, pass);

  if ( status != WL_CONNECTED) {
    Serial.println("Couldn't get a wifi connection");
    while(true);
  }
  else {
    Serial.println("Connected to network");
    ip = WiFi.localIP();
    Serial.println(ip);
  }
}

void loop() {
}
```
[1]: I tried just closing it because of https://trac.macports.org/ticket/43228
