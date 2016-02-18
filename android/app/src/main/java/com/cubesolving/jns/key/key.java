/* key: unlock door over udp
 Joshua Satterfield and Marcus
 code based on https://code.google.com/p/boxeeremote/wiki/AndroidUDP
 */
package com.cubesolving.jns.key;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static javax.crypto.Cipher.DECRYPT_MODE;

public class key extends AppCompatActivity {
    /* IV -- Initialization Vector. Should be random and unique. Used as the
    starting state of the AES algorithm. Not Secret. Should receive it from
    the Arduino app.
     */
    byte iv[] = {0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
    /* AES key. Secret. Should be same as key in Arduino program and it
    should be random. Java uses unsigned bytes so I can't store high literals
    like 0xa7. Now it works if you cast to byte, if interpreted in java it
    could end up negative -- but we don't care, all the bits are there whether
     you interpret the leftmost bit as a sign bit or as a number
     */
    byte key[] = {
            (byte) 0xa7, (byte) 0x74, (byte) 0xde, (byte) 0x71,
            (byte) 0xad, (byte) 0x17, (byte) 0x1d, (byte) 0xbd,
            (byte) 0xed, (byte) 0x3d, (byte) 0x4c, (byte) 0xc9,
            (byte) 0x55, (byte) 0xea, (byte) 0xb6, (byte) 0xb9,

            (byte) 0x1c, (byte) 0x0b, (byte) 0xed, (byte) 0x76,
            (byte) 0x63, (byte) 0x2d, (byte) 0x69, (byte) 0x42,
            (byte) 0xe7, (byte) 0xd5, (byte) 0x00, (byte) 0xd5,
            (byte) 0x58, (byte) 0x19, (byte) 0xbe, (byte) 0xb4,
//            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    };

    /* TODO: use as receiving array */
    byte plain[] = {
            // 0xf3, 0x44, 0x81, 0xec, 0x3c, 0xc6, 0x27, 0xba, 0xcd, 0x5d, 0xc3, 0xfb, 0x08, 0xf2, 0x73, 0xe6
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    };
    byte cipher[];
    static final int port = 2390;

    /* name of program, used for logging (Log.d(), etc) */
    private static final String TAG = "key";

    /* global socket, used by whole program for sending and receiving. */
    DatagramSocket socket;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);

        Button b_lock = (Button) findViewById(R.id.b_lock);
        Button b_unlock = (Button) findViewById(R.id.b_unlock);
        Button b_auth = (Button) findViewById(R.id.b_auth);

        /* aessend for encryption, send for unencrypted */
        b_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aessend("l");
            }
        });
        b_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aessend("u");
            }
        });
        b_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new send().execute("a");
                aessend("a");
            }
        });

        /* create socket now so both send and receive methods can use it later */
        try {
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
        } catch (Exception e) {
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

    /* https://code.google.com/p/boxeeremote/wiki/AndroidUDP
   used in send() */
    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp != null) openOptionsMenu(); /* so they can turn on wifi */

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "key Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.cubesolving.jns.key/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "key Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.cubesolving.jns.key/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /* http://developer.android.com/reference/android/os/AsyncTask.html */
    private class send extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... strings) {
            int count = strings.length;
            try {
                //InetAddress ip = getBroadcastAddress();
                for (int i = 0; i < count; i++) {
                    String data = strings[i];
                    socket.setBroadcast(true);
                    DatagramPacket packet =
                            new DatagramPacket(data.getBytes(), data.length(),
                                    getBroadcastAddress(), port);
                    socket.send(packet);
                }
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
            }
            return null; /* func is of type Void (the object).  */
        }
    }

    /* http://developer.android.com/reference/android/os/AsyncTask.html */
    private class sendbytes extends AsyncTask<byte[], Void, Void> {
        protected Void doInBackground(byte[]... bytes) {
            int count = bytes.length;
            try {
                //InetAddress ip = getBroadcastAddress();
                for (int i = 0; i < count; i++) {
                    byte[] data = bytes[i];
                    socket.setBroadcast(true);
                    DatagramPacket packet =
                            new DatagramPacket(data, data.length,
                                    getBroadcastAddress(), port);
                    socket.send(packet);
                }
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
            }
            return null; /* func is of type Void (the object).  */
        }
    }

    DatagramPacket receive() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return packet;
    }

    void aessend(String cmd) {
        new send().execute(cmd); /* auth */
//        new send().execute("a"); /* auth */
        /* get iv */
//        try {
//            //DatagramPacket pack = receive();
//            byte[] buf = new byte[1024];
//            DatagramPacket packet = new DatagramPacket(buf, buf.length);
//            socket.receive(packet);
//
//            Log.d(TAG, String.valueOf(packet));
//        } catch (Exception e) {
//            Log.e(TAG, "didn't get response from socket");
//        }
//
//        try {
//            SecretKeySpec k = new SecretKeySpec(key, "AES");
//
//            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
//            c.init(DECRYPT_MODE, k, new IvParameterSpec(iv));
//            /* hopefully 128 bits. encrypt cmd */
//            byte[] ciphertext = c.doFinal(cmd.getBytes());
//            new sendbytes().execute(ciphertext); /* TODO: encrypt */
//        } catch (NoSuchAlgorithmException e1) {
//            e1.printStackTrace();
//        } catch (NoSuchPaddingException e2) {
//            e2.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        }

    }
}