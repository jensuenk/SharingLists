package com.example.sharinglists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharinglists.models.ListModel;
import com.example.sharinglists.utils.TimeFormat;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference fListDatabase;
    private DatabaseReference fDatabase;
    private DatabaseReference fSharesDatabase;

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

    private void Logout() {
        fAuth.signOut();
        finish();
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    private void ShowSharingCode() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("EditText", fAuth.getCurrentUser().getUid());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this, "Code has been copied", Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(fAuth.getCurrentUser().getUid())
                .setTitle("Enter this code on the other device")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.LogoutMenu: {
                Logout();
                break;
            }
            case R.id.SharingCode: {
                ShowSharingCode();
                break;
            }
            case R.id.Refresh: {
                showLists();
                break;
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
        } else {
            fDatabase = FirebaseDatabase.getInstance().getReference();
            fListDatabase = fDatabase.child("Lists");
            fSharesDatabase = fDatabase.child("Shares");

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

        // Create lists table
        Map listMap = new HashMap();
        listMap.put("title", title);
        listMap.put("owner-uid", fAuth.getCurrentUser().getUid());
        listMap.put("owner-email", fAuth.getCurrentUser().getEmail());
        listMap.put(("id"), fnewListDatabase.getKey());
        listMap.put("timestamp", ServerValue.TIMESTAMP);

        fnewListDatabase.setValue(listMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Create shares table
        final DatabaseReference fnewShareDatabase = fDatabase.child("Shares").push();

        Map shareMap = new HashMap();
        shareMap.put("uid", fAuth.getCurrentUser().getUid());
        shareMap.put("id", fnewListDatabase.getKey());

        fnewShareDatabase.setValue(shareMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Create favorites table
        final DatabaseReference fnewFavoritesDatabase = fDatabase.child("Users").child(fAuth.getCurrentUser().getUid()).child("favorites").push();

        Map favoriteMap = new HashMap();
        favoriteMap.put("id", fnewListDatabase.getKey());
        favoriteMap.put("value", false);

        fnewFavoritesDatabase.setValue(favoriteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        progressDialog.dismiss();
        showLists();
    }

    public void showLists() {

        Toast.makeText(this, "Retrieving lists, please wait...", Toast.LENGTH_SHORT).show();

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
            protected void onBindViewHolder(final ListViewHolder viewHolder, final int position, ListModel model) {

                fSharesDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot shareSnapshot : dataSnapshot.getChildren()) {
                            try {
                                if (shareSnapshot.getKey().equals(getRef(position).getKey())) {
                                    if (shareSnapshot.child("uid").getValue().toString().equals(fAuth.getCurrentUser().getUid())) {

                                        final String listId = shareSnapshot.child("id").getValue().toString();

                                        fListDatabase.child(listId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("owner-email")) {
                                                    final String title = dataSnapshot.child("title").getValue().toString();
                                                    final String owner = dataSnapshot.child("owner-email").getValue().toString();
                                                    final String timestamp = dataSnapshot.child("timestamp").getValue().toString();

                                                    viewHolder.setListTitle(title);

                                                    if (owner.equals(fAuth.getCurrentUser().getEmail())) {
                                                        viewHolder.setListOwner("Me");
                                                    } else {
                                                        viewHolder.setListOwner("Shared by: " + owner);
                                                    }

                                                    viewHolder.setListDate("Edited " + TimeFormat.getTimeAgo(Long.parseLong(timestamp)));


                                                    fDatabase.child("Users").child(fAuth.getCurrentUser().getUid()).child("favorites").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            for (final DataSnapshot favoritesSnapshot : dataSnapshot.getChildren()) {
                                                                if (favoritesSnapshot.hasChild("id") && favoritesSnapshot.hasChild("value")) {
                                                                    if (favoritesSnapshot.child("id").getValue().equals(listId)) {
                                                                        viewHolder.setStar(Boolean.valueOf(favoritesSnapshot.child("value").getValue().toString()));

                                                                        viewHolder.star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                                            @Override
                                                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                                                viewHolder.setStar(isChecked);
                                                                                if (favoritesSnapshot.child("id").getValue().equals(listId)) {
                                                                                    favoritesSnapshot.child("value").getRef().setValue(isChecked);
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });


                                                    viewHolder.listCard.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent intent = new Intent(MainActivity.this, ItemsActivity.class);
                                                            intent.putExtra("listId", listId);
                                                            intent.putExtra("title", title);
                                                            intent.putExtra("ownerUid", dataSnapshot.child("owner-uid").getValue().toString());
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
                                }
                            } catch (Exception e) {
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "ERROR: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        ListsList.setAdapter(firebaseRecyclerAdapter);
    }

    private void deleteList(final String itemId) {

        fSharesDatabase.child(itemId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
