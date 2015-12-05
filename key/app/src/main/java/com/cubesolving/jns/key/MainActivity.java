/* key: unlock door over udp
 Joshua
 */
package com.cubesolving.jns.key;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    static final int port = 2390;
    //static final int serverip = 0;
    private static final String TAG = "key";
    DatagramSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b_lock = (Button) findViewById(R.id.b_lock);
        Button b_unlock = (Button) findViewById(R.id.b_unlock);
        Button b_auth = (Button) findViewById(R.id.b_auth);

        b_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new send().execute("l");
            }
        });
        b_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new send().execute("u");
            }
        });
        b_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new send().execute("a");
            }
        });

        try {
            socket = new DatagramSocket(port);
        } catch (Exception e) {
        }
    }


    /* https://code.google.com/p/boxeeremote/wiki/AndroidUDP */
    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (!dhcp) openOptionsMenu(); /* so they can turn on wifi */

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    String getudp() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        return socket.receive(packet);

    }

    /* see
    https://code.google.com/p/boxeeremote/wiki/AndroidUDP
    http://developer.android.com/reference/android/os/AsyncTask.html */
    private class send extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... strings) {
            int count = strings.length;
            try {
                //DatagramSocket sock = new DatagramSocket();
                //InetAddress ip = InetAddress.getByName("192.168.1.129");
                //InetAddress ip = InetAddress.getByName("192.168.1.114");
                InetAddress ip = getBroadcastAddress();
                for (int i = 0; i < count; i++) {
                    String data = strings[i];
                    //DatagramSocket socket = new DatagramSocket(port);
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                            getBroadcastAddress(), port);
                    socket.send(packet);

//                    Log.d(TAG, "jns-key send");
//                    String str = strings[i];
//                    byte[] bytes = str.getBytes();
//                    int len = str.length();
//                    DatagramPacket pack  = new DatagramPacket(bytes, len, ip, port);
//                    sock.send(pack); /* SEND */
//                    sock.close();

                }
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
            }
            return null; /* func is of type Void (the object) */
        }
    }
}
