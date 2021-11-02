package com.example.climalert;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;

public class Auth_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        final Button button = findViewById(R.id.SignInGoogle);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              setContentView(R.layout.activity_main);
            }
        });
    }
}