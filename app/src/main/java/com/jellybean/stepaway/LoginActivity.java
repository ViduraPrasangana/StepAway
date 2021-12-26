package com.jellybean.stepaway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    EditText phone,name;
    Button sendBtn;
    PinView otpView;

    FirebaseAuth firebaseAuth;

    String verificationId;
    String finalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        phone = findViewById(R.id.phone);
        name = findViewById(R.id.name);
        sendBtn = findViewById(R.id.sendBtn);
        otpView = findViewById(R.id.otp_view);

        firebaseAuth = FirebaseAuth.getInstance();

        phone.setText("+94");
        Selection.setSelection(phone.getText(), phone.getText().length());

        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("+94")){
                    phone.setText("+94");
                    Selection.setSelection(phone.getText(), phone.getText().length());
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this,"Please enter a name",Toast.LENGTH_LONG).show();
                }else{
                    finalName = name.getText().toString();
                    name.setEnabled(false);
                    verifyPhone(phone.getText().toString());
                }

            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==6){
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, s.toString());
                    signIn(credential);
                }
            }
        };
        otpView.addTextChangedListener(textWatcher);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startMainActivity();
            finish();
        }
    }

    public void signIn(PhoneAuthCredential credential){
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information

                                if(firebaseAuth.getCurrentUser() != null){
                                    FirebaseDatabase d = FirebaseDatabase.getInstance();
                                    d.getReference().child("users").child(firebaseAuth.getCurrentUser().getPhoneNumber()).setValue(finalName);
                                    startMainActivity();
                                }
                            } else {
                                // Sign in failed, display a message and update the UI
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(LoginActivity.this,"Login failed",Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
    }

    private void startMainActivity() {
        Intent i = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(i);
        finish();
    }

    public void verifyPhone(String phone){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);
                                otpView.setEnabled(true);
                                LoginActivity.this.verificationId = verificationId;
                            }

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                otpView.setText(phoneAuthCredential.getSmsCode());
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(LoginActivity.this,"Verification Failed",Toast.LENGTH_LONG).show();
                            }
                        })          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}