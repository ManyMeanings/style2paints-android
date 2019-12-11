package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

public class FirstPageActivity extends AppCompatActivity {

    private Button bTnStarted;
    private Button bTnSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firstpage);
        bTnStarted = findViewById(R.id.btn_login);
        bTnStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstPageActivity.this, SecondPageActivity.class);
                startActivity(intent);
            }
        });
    }
}
