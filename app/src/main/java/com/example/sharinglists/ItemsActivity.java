package com.example.sharinglists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharinglists.models.ItemModel;
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

public class ItemsActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference fDatabase;
    private DatabaseReference fSharesDatabase;
    private DatabaseReference fListDatabase;
    private DatabaseReference fItemDatabase;

    private RecyclerView itemList;
    private Toolbar toolbar;

    private ProgressDialog progressDialog;

    private EditText listTitle;
    private Menu menu;

    private String listId;
    private String title;
    private String ownerUid;

    private FirebaseRecyclerAdapter<ItemModel, ItemViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        Intent intent = getIntent();
        listId = intent.getStringExtra("listId");
        title = intent.getStringExtra("title");
        ownerUid = intent.getStringExtra("ownerUid");

        itemList = findViewById(R.id.main_lists_list);
        itemList.setHasFixedSize(true);
        itemList.setLayoutManager(new LinearLayoutManager(this));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(itemList);

        listTitle = findViewById(R.id.items_list_title);
        listTitle.setText(title);
        listTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                fListDatabase.child("title").setValue(listTitle.getText().toString().trim());
            }
        });

        fAuth = FirebaseAuth.getInstance();
        //fSharesDatabase = FirebaseDatabase.getInstance().getReference().child("Shares").child(fAuth.getCurrentUser().getUid()).child(listId).child("shares");
        //fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid()).child(listId);
        //fItemDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid()).child(listId).child("items");
        fDatabase = FirebaseDatabase.getInstance().getReference();
        fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(listId);
        fItemDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(listId).child("items");



        toolbar = findViewById(R.id.share_button);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        showItems();
    }

    public void newItem(View view) {
        DatabaseReference fNewItemDatabase = fItemDatabase.push();

        Map listMap = new HashMap();
        listMap.put("name", "");
        listMap.put("check", false);

        fNewItemDatabase.setValue(listMap);
    }

    private void showItems() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retreiving items, please wait...");
        progressDialog.show();

        Query query = fItemDatabase.orderByValue();
        FirebaseRecyclerOptions<ItemModel> options = new FirebaseRecyclerOptions.Builder<ItemModel>()
                .setQuery(query, ItemModel.class)
                .setLifecycleOwner(this)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ItemModel, ItemViewHolder>(options) {

            @Override
            public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_listitem_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(final ItemViewHolder viewHolder, int position, ItemModel model) {
                final String itemId = getRef(position).getKey();
                fItemDatabase.child(itemId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("check")) {
                            String title = dataSnapshot.child("name").getValue().toString();
                            String check = dataSnapshot.child("check").getValue().toString();

                            viewHolder.setItemName(title);
                            viewHolder.itemName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (!hasFocus) {
                                        updateItems(viewHolder, itemId);
                                    }
                                }
                            });

                            viewHolder.setCheckBox(Boolean.parseBoolean(check));
                            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateItems(viewHolder, itemId);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ItemsActivity.this, "ERROR: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        itemList.setAdapter(firebaseRecyclerAdapter);
        progressDialog.dismiss();
    }

    private void updateItems(ItemViewHolder viewHolder, String itemId) {

        Map updateMap = new HashMap();
        updateMap.put("name", viewHolder.getItemName().getText().toString().trim());
        updateMap.put("check", String.valueOf(viewHolder.getCheckBox().isChecked()));

        fItemDatabase.child(itemId).updateChildren(updateMap);

        Log.i("ItemsActivity", "item " + itemId + " updated");
    }

    private void deleteItem(String itemId) {

        fItemDatabase.child(itemId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ItemsActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ItemsActivity ", task.getException().toString());
                    Toast.makeText(ItemsActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
            deleteItem(firebaseRecyclerAdapter.getRef(viewHolder.getAdapterPosition()).getKey());
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.share_menu, menu);
        this.menu  = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        else if (item.getItemId() == R.id.share_button) {
            createShareDialog();
        }
        return true;
    }

    /*
    public void share() {

        DatabaseReference fNewShareDatabase = fSharesDatabase.push();

        Map listMap = new HashMap();
        listMap.put("uid", fAuth.getCurrentUser().getUid());

        fNewShareDatabase.setValue(listMap);
    }

     */


    public void createShareDialog() {
        if (ownerUid.equals(fAuth.getCurrentUser().getUid())) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_input, null);
            dialogBuilder.setView(dialogView);

            final EditText code = dialogView.findViewById(R.id.dialog_edittext);

            dialogBuilder.setTitle("Enter share code");
            dialogBuilder.setMessage("Enter the share code of the person you want to share this list with");
            dialogBuilder.setPositiveButton("Share", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {

                    newShare(code);
                }
            });

            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ItemsActivity.this);
            builder.setMessage("Ask the owner of this list to add more users")
                    .setTitle("No Permissons");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    private void newShare(EditText titleEditText) {

        final String code = titleEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "ERROR: Fill in a code!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sharing list, please wait...");
        progressDialog.show();

        DatabaseReference fShareDatabase = fDatabase.child("Shares").push();

        Map listMap = new HashMap();
        listMap.put("uid", code);
        listMap.put("id", listId);

        fShareDatabase.setValue(listMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    progressDialog.dismiss();

                    Toast.makeText(ItemsActivity.this, "Share successful.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ItemsActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
