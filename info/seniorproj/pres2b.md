lock
====
Joshua Satterfield and Marcus Alexander

---

unlocks door with smartphone
* send packet from phone
* wifi shield receives
* arduino turns motor

---

packets
* arduino sends challenge
* phone sends challenge + command
* encrypt with with aes
* send over udp

---

arduino
* wifi
* aes
* servo

---

phone
* android
* async func for networking

---

net

	private class send extends AsyncTask<String, Void, Void> {
	        protected Void doInBackground(String... strings) {
	            int count = strings.length;
	            try {
	                DatagramSocket sock = new DatagramSocket();
	                InetAddress ip = InetAddress.getByName("192.168.1.129");

---

	                for (int i = 0; i < count; i++) {
	                    String str = strings[i];
	                    byte[] bytes = str.getBytes();
	                    int len = str.length();
	                    DatagramPacket pack = new DatagramPacket(bytes, len, ip, port);
	                    sock.send(pack); /* SEND */
	                    sock.close();
	                }

---

	            } catch (Exception e) {
	                Log.e(TAG, "exception", e);
	            }
	        return null; /* func is of type Void (the object) */
	         }
	    }
	}


---

onCreate

 	Button b_lock = (Button) findViewById(R.id.b_lock);

        b_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new send().execute("l");
            }
        });

---

arduino loop

	  char c = pack[0];
	  if (c == 'l') /* lock */
	    servo.write(180);
	  else if (c == 'u') /* unlock */
	    servo.write(0);

---

	  else if (c == 'r') {
	    randnum = random(999999);
	    Serial.println(randnum);
	  } else {
	    Serial.print("err: did not understand command: ");
	    Serial.print(pack);
	    Serial.print("<-\n");
	  }


---

questions?
