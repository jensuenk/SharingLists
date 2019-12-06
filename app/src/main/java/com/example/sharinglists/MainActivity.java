package com.example.sharinglists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharinglists.models.ListModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference fListDatabase;

    private RecyclerView mListsList;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListsList = (RecyclerView) findViewById(R.id.main_lists_list);
        mListsList.setHasFixedSize(true);
        mListsList.setLayoutManager(new LinearLayoutManager(this));

        fAuth = FirebaseAuth.getInstance();

        updateActivivty();

        if (fAuth != null) {
            fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid());
            showLists();
        }
    }


    private void updateActivivty() {
        // Check if user is logged in
        if (fAuth.getCurrentUser() == null) {
            Intent startIntent = new Intent(this, StartActivity.class);
            startActivity(startIntent);
            finish();
        }
    }

    public void newList(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_input, null);
        dialogBuilder.setView(dialogView);

        final EditText title = dialogView.findViewById(R.id.dialog_edittext);

        dialogBuilder.setTitle("Enter a title");
        dialogBuilder.setMessage("Enter a name for your new list below");
        dialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int whichButton) {

                createList(title);
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public void createList(EditText titleEditText) {
        final String title = titleEditText.getText().toString().trim();

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
                    Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                    Toast.makeText(MainActivity.this, "List successfully created.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText( MainActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    public void showLists() {
        Toast.makeText(this, "Retrieving lists, please wait...", Toast.LENGTH_SHORT).show();


        Query query = fListDatabase.orderByValue();
        FirebaseRecyclerOptions<ListModel> options = new FirebaseRecyclerOptions.Builder<ListModel>()
                .setQuery(query, ListModel.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<ListModel, ListViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ListModel, ListViewHolder>(options) {

                    @Override
                    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
                    {
                        return new ListViewHolder(LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.single_list_layout, parent, false));
                    }

                    @Override
                    protected void onBindViewHolder(final ListViewHolder viewHolder, int position, ListModel model)
                    {
                        final String listId = getRef(position).getKey();
                        fListDatabase.child(listId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("title"))
                                {
                                    String title = dataSnapshot.child("title").getValue().toString();

                                    viewHolder.setListTitle(title);


                                    viewHolder.listCard.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(MainActivity.this, ItemsActivity.class);
                                            intent.putExtra("listId", listId);
                                            startActivity(intent);
                                        }
                                    });


                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MainActivity.this, "ERROR: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };
        mListsList.setAdapter(firebaseRecyclerAdapter);
    }
}
