package com.jellybean.stepaway;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chaos.view.PinView;

public class LoginActivity extends AppCompatActivity {
    EditText phone;
    Button sendBtn;
    PinView otpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        phone = findViewById(R.id.phone);
        sendBtn = findViewById(R.id.sendBtn);
        otpView = findViewById(R.id.otp_view);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otpView.setEnabled(true);
            }
        });
    }
}