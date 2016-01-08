lock
====

Unlock a door from your phone.
Uses an Android app to send commands over UDP to and Arduino.

## use
Patch your arduino library to use printf if needed (used in current version) using this guide http://playground.arduino.cc/Main/Printf
Set up motor on pin 9.
Upload udp/udp.ino sketch to Arduino with WiFi shield.
Run arduino/ app on phone

## folders
```
ionic	old app draft
android	current phone app
udp	arduino program
AES-library	...
Makefile	not in use, for uploading arduino code without IDE
```

# encryption
AES CBC in progress 

[X] Send random IV

# screenshot
![android key app screenshot](key.png)

Thanks to Dr. Lin for the guidance and Marcus for the hardware

