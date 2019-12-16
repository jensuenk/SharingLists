package com.example.sharinglists.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharinglists.MainActivity;
import com.example.sharinglists.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private EditText passwordEail;
    private Button resetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);


        passwordEail = (EditText)findViewById(R.id.etPasswordlayout);
        resetPassword = (Button)findViewById(R.id.btnPasswordReset);
        fAuth = FirebaseAuth.getInstance();

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = passwordEail.getText().toString().trim();

                if (useremail.equals("")){
                    Toast.makeText(PasswordActivity.this,"please enter your registered email id",Toast.LENGTH_LONG).show();
                }else {
                    fAuth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(PasswordActivity.this,"password reset email send", Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(PasswordActivity.this, LoginActivity.class));
                            }else {
                                Toast.makeText(PasswordActivity.this,"error in sending password reset email", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
            }
        });

    }
}
