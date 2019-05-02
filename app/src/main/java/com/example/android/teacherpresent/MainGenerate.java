package com.example.android.teacherpresent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MainGenerate extends AppCompatActivity {

    EditText text;
    EditText date;
    Button gen_btn;
    ImageView image;
    String text2Qr;
    Spinner spinner;

    private Button mlogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_generate);

        text = (EditText) findViewById(R.id.text);
        gen_btn = (Button) findViewById(R.id.gen_btn);
        image = (ImageView) findViewById(R.id.image);
        spinner=findViewById(R.id.spinner1);

        mlogout=findViewById(R.id.logout);


        mlogout.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {
                FirebaseAuth fAuth = FirebaseAuth.getInstance();
                fAuth.signOut();
                startActivity(new Intent(MainGenerate.this, MainActivity.class));
            }
        });

        String[] items=new String[]{"CSE 4501","CSE 4512","CSE4513"};

        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items);
        spinner.setAdapter(adapter);

        gen_btn.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {

                text2Qr = text.getText( ).toString( ).trim( )+" "+spinner.getSelectedItem().toString().trim();
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter( );
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE, 200, 200);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    image.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace( );
                }
            }

        });
    }
}
