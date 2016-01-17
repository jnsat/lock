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
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class key extends AppCompatActivity {
    byte iv[] = { 0x00, 0x00, 0x00, 0x00, 0x00,
                  0x00, 0x00, 0x00, 0x00, 0x00,
                  0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
    /* Should be same as Ar ino and random. Java uses unsigned bytes so I can't store high literals like 0xa7.
       Now it works if you cast to byte, if interpreted in java it could end up negative -- but we don't care
       all the bits are there weather you interpret the leftmost bit as a sign bit or as a number
     */
    byte key[] = {
            //0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            //0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            (byte) 0xa7, (byte) 0x74, (byte) 0xde, (byte) 0x71,
            (byte) 0xad, (byte) 0x17, (byte) 0x1d, (byte) 0xbd,
            (byte) 0xed, (byte) 0x3d, (byte) 0x4c, (byte) 0xc9,
            (byte) 0x55, (byte) 0xea, (byte) 0xb6, (byte) 0xb9,

            (byte) 0x1c, (byte) 0x0b, (byte) 0xed, (byte) 0x76,
            (byte) 0x63, (byte) 0x2d, (byte) 0x69, (byte) 0x42,
            (byte) 0xe7, (byte) 0xd5, (byte) 0x00, (byte) 0xd5,
            (byte) 0x58, (byte) 0x19, (byte) 0xbe, (byte) 0xb4,
    } ;
    byte plain[] = {
    // 0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    };
    byte cipher[];
    static final int port = 2390;
    //static final int serverip = 0;
    private static final String TAG = "key";
    DatagramSocket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);

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
        } catch (Exception e) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_key, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    /* https://code.google.com/p/boxeeremote/wiki/AndroidUDP */
    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        //if (!dhcp != Null) openOptionsMenu(); /* so they can turn on wifi */

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    DatagramPacket receive() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return packet;
    }

    void aessend(byte[] iv, String cmd) {

        try {
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            //c.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(iv));
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            //Aplain = c.doFinal(cipher)
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }
}
