package com.posmobileuno;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.posmobileuno.btdroid.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Gerenciar extends Activity {
    private static final String TAG = "BT";

    Button at1, at2, servoAuto;
    static int fAt1 = 0;
    static int fAt2 = 0;
    static int fServo = 0;
    SeekBar skbarServo;
    TextView txtAngulo;

    TextView txtArduino;
    Handler h;

    final int RECIEVE_MESSAGE = 1; // Status for Handler

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // Well known SPP UUID
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Insert your bluetooth devices MAC address
    private static String address = "00";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "In onCreate()");
        Log.d("MAC", address);

        address = getIntent().getStringExtra("MAC");
        Log.d("MAC", address);

        setContentView(R.layout.main);

        at1 = (Button) findViewById(R.id.btnAt1);
        at2 = (Button) findViewById(R.id.btnAt2);
        servoAuto = (Button) findViewById(R.id.btnServo);
        txtAngulo = (TextView) findViewById(R.id.txtAngulo);
        txtArduino = (TextView) findViewById(R.id.txtBTread);

        skbarServo = (SeekBar) findViewById(R.id.skbar_servo);

        skbarServo.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {

                String value = String.valueOf(skbarServo.getProgress() + 100);
                txtAngulo.setText("Ângulo = " + progress + "°");
                Log.d("SKB", String.valueOf(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                String value = String.valueOf(skbarServo.getProgress() + 100);
                mConnectedThread.write("S1" + value + ";");
            }
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        at1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (fAt1 == 0) {
                    mConnectedThread.write("A11;");
                    fAt1 = 1;
                    at1.setText("Atuador 1 - On");
                    at1.setBackgroundColor(Color.parseColor("#FF0000"));
                } else if (fAt1 == 1) {
                    mConnectedThread.write("A10;");
                    fAt1 = 0;
                    at1.setText("Atuador 1 - Off");
                    at1.setBackgroundColor(Color.parseColor("#00FF00"));
                }
            }
        });

        at2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (fAt2 == 0) {
                    mConnectedThread.write("A21;");
                    fAt2 = 1;
                    at2.setText("Atuador 2 - On");
                    at2.setBackgroundColor(Color.parseColor("#FF0000"));
                } else if (fAt2 == 1) {
                    mConnectedThread.write("A20;");
                    fAt2 = 0;
                    at2.setText("Atuador 2 - Off");
                    at2.setBackgroundColor(Color.parseColor("#00FF00"));
                }
            }
        });

        servoAuto.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (fServo == 0) {
                    mConnectedThread.write("S15;");
                    fServo = 1;
                    servoAuto.setText("Servo Auto - On");
                    servoAuto.setBackgroundColor(Color.parseColor("#FF0000"));
                } else if (fServo == 1) {
                    mConnectedThread.write("S16;");
                    fServo = 0;
                    servoAuto.setText("Servo Auto - Off");
                    servoAuto.setBackgroundColor(Color.parseColor("#00FF00"));
                }
            }
        });

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.d("BT", "recebeu" + msg.what);
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                    // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        Log.d("BT", "recebeu st" + strIncom);// create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);                // extract string
                            sb.delete(0, sb.length());                                        // and clear
                            txtArduino.setText("Data from Arduino: " + sbprint);            // update TextView

                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            }

            ;
        };

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...In onResume - Attempting client connect...");
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        // A MAC address, which we got above.
        // A Service ID or UUID. In this case we are using the
        // UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: "
                    + e.getMessage() + ".");
        }

        // Discovery is resource intensive. Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection. This will block until it connects.
        Log.d(TAG, "...Connecting to Remote...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error",
                        "In onResume() and unable to close socket during connection failure"
                                + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit(
                    "Fatal Error",
                    "In onResume() and output stream creation failed:"
                            + e.getMessage() + ".");
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit(
                        "Fatal Error",
                        "In onPause() and failed to flush output stream: "
                                + e.getMessage() + ".");
            }
        }

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket."
                    + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned
        // on

        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                // Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(
                        btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast msg = Toast.makeText(getBaseContext(), title + " - " + message,
                Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }


    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256]; // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer); // Get number of bytes and
                    // message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer)
                            .sendToTarget(); // Send to message queue Handler
                    Log.d(TAG, "...Data to Android: " + bytes + "...");
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
}
