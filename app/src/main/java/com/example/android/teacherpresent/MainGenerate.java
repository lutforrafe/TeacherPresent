package com.example.android.teacherpresent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainGenerate extends AppCompatActivity {

    TextView text;
    EditText date;
    TextView QRText;

    ImageView image;
    String text2Qr;
    Spinner spinnerCourse,spinnerId;

    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    Thread serverThread = null;
    public static final int SERVER_PORT = 3003;
    private Handler handler;
    private int greenColor;
    private LinearLayout msgList;

    private Button mlogout;
    private Button gen_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_generate);

        text = findViewById(R.id.text);
        gen_btn = findViewById(R.id.gen_btn);
        image = findViewById(R.id.image);
        spinnerCourse=findViewById(R.id.spinner1);
        spinnerId=findViewById(R.id.spinner2);
        mlogout=findViewById(R.id.logout);
        QRText= findViewById(R.id.QRText);
        QRText.setVisibility(View.INVISIBLE);

        greenColor = ContextCompat.getColor(this, R.color.green);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);



        mlogout.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                FirebaseAuth fAuth = FirebaseAuth.getInstance();
                fAuth.signOut();
                startActivity(new Intent(MainGenerate.this, MainActivity.class));
            }
        });

        String[] items1=new String[]{"","4501","4502","4503"};
        String[] items2=new String[]{"","CSE","EEE","CEE","MCE","MAT","HUM","BTM","TVE"};

        ArrayAdapter<String> adapter1=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items1);
        spinnerCourse.setAdapter(adapter1);
        ArrayAdapter<String> adapter2=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items2);
        spinnerId.setAdapter(adapter2);

        spinnerCourse.setSelection(1);
        spinnerId.setSelection(1);

        gen_btn.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                final String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());


                if(!ipAddress.equals("0.0.0.0")){
                    text.setText(ipAddress);
                    text2Qr = ipAddress+"."+spinnerId.getSelectedItemId()+"."+spinnerCourse.getSelectedItem().toString()+"."+1;
                    QRText.setText(text2Qr);
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter( );
                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE, 200, 200);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        image.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace( );
                    }
                    msgList.removeAllViews();
                    showMessage("Server Started.", Color.BLACK);
                    serverThread = new Thread(new ServerThread());
                    serverThread.start();
                }
                else{
                    text.setText("Not connected");
                }

            }

        });
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }
    String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:YYYY");
        return sdf.format(new Date());
    }

    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message + " [" + getTime() +"]");
        tv.setTextSize(10);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }
    public void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(message, color));
            }
        });
    }
    private void sendMessage(final String message) {
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out.println(message);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                findViewById(R.id.gen_btn).setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), Color.RED);
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showMessage("Error Communicating to Client :" + e.getMessage(), Color.RED);
                    }
                }
            }
        }
    }
    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Connecting to Client!!", Color.RED);
            }
            showMessage("Connected to Client!!", greenColor);
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (null == read || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "Client Disconnected";
                        showMessage("Client : " + read, greenColor);
                        break;
                    }
                    //Store the following read string to database
                    showMessage("Client : " + read, greenColor);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}
