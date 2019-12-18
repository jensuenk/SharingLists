package com.example.sharinglists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharinglists.models.ListModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference fListDatabase;
    private DatabaseReference fDatabase;

    private RecyclerView ListsList;

    private ProgressDialog progressDialog;

    private FirebaseRecyclerAdapter<ListModel, ListViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListsList = findViewById(R.id.main_lists_list);
        ListsList.setHasFixedSize(true);
        ListsList.setLayoutManager(new LinearLayoutManager(this));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(ListsList);

        fAuth = FirebaseAuth.getInstance();

        updateActivity();
    }
    private void Logout(){
        fAuth.signOut();
        finish();
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.LogoutMenu:{
                Logout();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateActivity() {
        // Check if user is logged in
        if (fAuth.getCurrentUser() == null) {
            Intent startIntent = new Intent(this, StartActivity.class);
            startActivity(startIntent);
            finish();
        }
        else {
            //fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid());
            fDatabase = FirebaseDatabase.getInstance().getReference();
            fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists");


            /*
            fListDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot listsSnapshot) {
                    for (DataSnapshot listSnapshot: listsSnapshot.getChildren()) {
                        Iterable<DataSnapshot> sharesSnapshot = listSnapshot.child("shares").getChildren();
                        if (listSnapshot.child("owner-uid").getValue().toString().equals(fAuth.getCurrentUser().getUid())) {
                            showList(listSnapshot.getKey());
                            break;
                        }

                        for (DataSnapshot shareSnapshot: sharesSnapshot) {
                            //Log.i("SHARETESTS", shareSnapshot.child("uid").getValue().toString());
                            //Log.i("SHARETESTS", fAuth.getCurrentUser().getUid());

                            if (shareSnapshot.child("uid").getValue().toString().equals(fAuth.getCurrentUser().getUid())) {
                                //Log.i("SHARETESTS", "EWAAAAA dit was" + shareSnapshot.getKey());
                                //Log.i("SHARETESTS", "Hunk " + shareSnapshot.getRef().getParent().getParent().toString());

                                showList(listSnapshot.getKey());
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("ERROR TESTJEEEEEEEEEEE", "onCancelled", databaseError.toException());
                }
            });

            //showLists();

             */
            showLists();
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

    private void createList(EditText titleEditText) {

        final String title = titleEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "ERROR: Fill in a title!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating list, please wait...");
        progressDialog.show();

        final DatabaseReference fnewListDatabase = fListDatabase.push();

        Map listMap = new HashMap();
        listMap.put("title", title);
        listMap.put("owner-uid", fAuth.getCurrentUser().getUid());
        listMap.put("owner-email", fAuth.getCurrentUser().getEmail());

        fnewListDatabase.setValue(listMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    fnewListDatabase.child("id").setValue(fnewListDatabase.getKey());
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "List successfully created.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Create shares table
    }

    public void showLists() {

    //private void showLists() {
        Toast.makeText(this, "Retrieving lists, please wait...", Toast.LENGTH_SHORT).show();


        /*
        for sharecodes van user

         */
        //Query query = fListDatabase.child(fAuth.getCurrentUser().getUid()).orderByChild("uid").equalTo(fAuth.getCurrentUser().getUid());

        //Query query = fListDatabase.orderByChild("owner-uid").equalTo(fAuth.getCurrentUser().getUid());
        Query query = fDatabase.child("Shares").orderByChild("uid").equalTo(fAuth.getCurrentUser().getUid());


        FirebaseRecyclerOptions<ListModel> options = new FirebaseRecyclerOptions.Builder<ListModel>()
                .setQuery(query, ListModel.class)
                .setLifecycleOwner(this)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ListModel, ListViewHolder>(options) {

            @Override
            public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ListViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_list_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(final ListViewHolder viewHolder, int position, ListModel model) {

                //fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists");

                final  String listId = getRef(position).getKey();


                DatabaseReference shareReference = fDatabase.child("Shares");//.child(getRef(position).toString());

                shareReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot listSnapshot: dataSnapshot.getChildren()) {
                            if (listSnapshot.child("uid").getValue().toString().equals(fAuth.getCurrentUser().getUid())) {
                                Log.i("TESTJEE ref:", listSnapshot.child("id").getValue().toString());
                            }
                        }

                        //Log.i("TESTJEE ref:", dataSnapshot.child("uid").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });





                fListDatabase.child(listId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("title")) {
                            final String title = dataSnapshot.child("title").getValue().toString();

                            viewHolder.setListTitle(title);


                            viewHolder.listCard.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(MainActivity.this, ItemsActivity.class);
                                    intent.putExtra("listId", listId);
                                    intent.putExtra("title", title);
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
        ListsList.setAdapter(firebaseRecyclerAdapter);
    }

    private void deleteList(String itemId) {

        fListDatabase.child(itemId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "List Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ItemsActivity ", task.getException().toString());
                    Toast.makeText(MainActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            deleteList(firebaseRecyclerAdapter.getRef(viewHolder.getAdapterPosition()).getKey());
        }
    };
}
