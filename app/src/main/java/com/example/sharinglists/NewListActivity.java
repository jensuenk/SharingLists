package com.example.sharinglists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharinglists.sign.LoginActivity;
import com.example.sharinglists.sign.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NewListActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference fListDatabase;

    private ProgressDialog progressDialog;

    private EditText inputTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_list);

        inputTitle = findViewById(R.id.input_newlist_title);

        fAuth = FirebaseAuth.getInstance();
        fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid());
    }

    public void createList(View view) {
        final String title = inputTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "ERROR: Fill in a title!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating list, please wait...");
        progressDialog.show();

        DatabaseReference fnewListDatabase = fListDatabase.push();

        Map listMap = new HashMap();
        listMap.put("title", title);

        fnewListDatabase.setValue(listMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent mainIntent = new Intent(NewListActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                    Toast.makeText(NewListActivity.this, "List successfully created.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText( NewListActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    public void newItem(View view) {
    }
}
