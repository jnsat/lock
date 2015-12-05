lock
====

Unlock a door from your phone.
Uses an Android app to send commands over UDP to and Arduino.

## use
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

Thanks to Dr. Lin for the guidance and Marcus for the hardware

