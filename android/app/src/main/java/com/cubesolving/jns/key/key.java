/* key: unlock door over udp
 Joshua
 */
package com.cubesolving.jns.key;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class key extends AppCompatActivity implements View.OnClickListener {
    static final int port = 2390;
    private static final String TAG = "key";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);

        Button b_lock = (Button) findViewById(R.id.b_lock);
        b_lock.setOnClickListener(this);
        Button b_unlock = (Button) findViewById(R.id.b_unlock);
        b_unlock.setOnClickListener(this);
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
//    View view = findViewById(android.R.id.content);
//    Button b_lock = (Button) findViewById(R.id.b_lock);
//    b_lock.setOnClickListener( new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            new send().execute("l");
//        }
//    });
//    Button b_unlock = (Button) findViewById(R.id.b_unlock);
//    b_unlock.setOnClickListener( new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            new send().execute("u");
//        }
//    });

    /* see
    https://code.google.com/p/boxeeremote/wiki/AndroidUDP
    http://developer.android.com/reference/android/os/AsyncTask.html */
    private class send extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... strings) {
            int count = strings.length;
            try {
                DatagramSocket sock = new DatagramSocket();
                for (int i = 0; i < count; i++) {
                    String str = strings[i];
                    byte[] bytes = str.getBytes();
                    int len = str.length();
                    InetAddress ip = InetAddress.getByName("192.168.1.129");
                    DatagramPacket pack = new DatagramPacket(bytes, len, ip, port);
                    sock.send(pack); /* SEND */
                }
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
            }
        return null; /* func is of type Void (the object) */
        }
    }
    public void onClick(View v) {
        char cmd = ' ';
        switch(v.getId()) {
            case R.id.b_lock:
                cmd = 'l';
                break;
            case R.id.b_unlock:
                cmd = 'u';
                break;
        }
        new send().execute(Character.toString(cmd));
    }
}
