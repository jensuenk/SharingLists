package com.example.sharinglists;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.sharinglists.user_sign.LoginActivity;
import com.example.sharinglists.StartActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();

        updateActivivty();
    }

    private void updateActivivty() {
        if (fAuth.getCurrentUser() == null) {
            Intent startIntent = new Intent(this, StartActivity.class);
            startActivity(startIntent);
            finish();
        }
    }
}
