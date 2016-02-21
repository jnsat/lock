From code.google.com/p/boxeeremote

# Introduction #

It took me forever to figure out how to send and receive UDP broadcast packets in android. This page explains how so others don't have to beat their heads against the wall.

# Details #

In my example, I need to send a broadcast packet on port 2562, as Boxees listens on that port and will respond with its own UDP packet announcing itself. This is how I discover where boxee lives.

Part of this project contains some [simple android code using UDP broadcast](http://code.google.com/p/boxeeremote/source/browse/trunk/Boxee+Remote/src/com/andrewchatham/Discoverer.java?spec=svn28&r=28), but that contains extraneous code, so I'll explain the important bits here.

The tricky bit is that sending a broadcast packet that includes the mobile network will silently fail, so you need to restrict yourself to the wifi network. Since the Android emulator doesn't support wifi, you have to test this all on a real device.

### Getting the Broadcast Address ###

You need to access the wifi manager to get the DHCP info and construct a broadcast address from that:

```
InetAddress getBroadcastAddress() throws IOException {
    WifiManager wifi = mContext.getSystemService(Context.WIFI_SERVICE);
    DhcpInfo dhcp = wifi.getDhcpInfo();
    // handle null somehow

    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
    byte[] quads = new byte[4];
    for (int k = 0; k < 4; k++)
      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
    return InetAddress.getByAddress(quads);
}
```

### Sending and Receiving UDP Broadcast Packets ###

Having constructed the broadcast address, things work as normal. The following code would send the string `data` over broadcast and then wait for a response:

```
DatagramSocket socket = new DatagramSocket(PORT);
socket.setBroadcast(true);
DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
    getBroadcastAddress(), DISCOVERY_PORT);
socket.send(packet);

byte[] buf = new byte[1024];
DatagramPacket packet = new DatagramPacket(buf, buf.length);
socket.receive(packet);
```
