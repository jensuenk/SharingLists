package com.example.sharinglists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharinglists.models.ItemModel;
import com.example.sharinglists.sign.RegisterActivity;
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
    private DatabaseReference fListDatabase;
    private DatabaseReference fItemDatabase;

    private RecyclerView mItemList;

    private ProgressDialog progressDialog;

    private TextView listTitle;

    private String listId;
    private String title;

    private FirebaseRecyclerAdapter<ItemModel, ItemViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        Intent intent = getIntent();
        listId = intent.getStringExtra("listId");
        title = intent.getStringExtra("title");

        mItemList = (RecyclerView) findViewById(R.id.main_lists_list);
        mItemList.setHasFixedSize(true);
        mItemList.setLayoutManager(new LinearLayoutManager(this));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mItemList);

        listTitle = findViewById(R.id.items_list_title);
        listTitle.setText(title);

        fAuth = FirebaseAuth.getInstance();
        fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid()).child(listId);
        fItemDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid()).child(listId).child("Items");

        showItems();
    }

    public void newItem(View view) {
        DatabaseReference fNewItemDatabase = fItemDatabase.push();

        Map listMap = new HashMap();
        listMap.put("name", "");
        listMap.put("check", false);

        fNewItemDatabase.setValue(listMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showItems();
                }
                else {
                    Toast.makeText( ItemsActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showItems()
    {
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
                    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
                    {
                        return new ItemViewHolder(LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.single_listitem_layout, parent, false));
                    }

                    @Override
                    protected void onBindViewHolder(final ItemViewHolder viewHolder, int position, ItemModel model)
                    {
                        final String itemId = getRef(position).getKey();
                        fItemDatabase.child(itemId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("check"))
                                {
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
        mItemList.setAdapter(firebaseRecyclerAdapter);
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

    String uid = "zcf3VZhjZtPsF54r7qw6IXpaWyg2";
    String email = "test@test.com";
    public void share() {

        fListDatabase.child("shares").child("uid").setValue(uid)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(ItemsActivity.this, "Shared with " + email + "!", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(ItemsActivity.this, "ERROR : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

}
