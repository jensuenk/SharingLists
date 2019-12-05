package com.example.sharinglists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

public class NewListActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference fListDatabase;
    private DatabaseReference fItemDatabase;

    private RecyclerView mItemList;

    private ProgressDialog progressDialog;

    private EditText inputTitle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_list);

        Intent intent = getIntent();
        String listId = intent.getStringExtra("listId");

        mItemList = (RecyclerView) findViewById(R.id.main_lists_list);
        mItemList.setHasFixedSize(true);
        mItemList.setLayoutManager(new LinearLayoutManager(this));

        inputTitle = findViewById(R.id.input_newlist_title);

        fAuth = FirebaseAuth.getInstance();
        fListDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid());
        fItemDatabase = FirebaseDatabase.getInstance().getReference().child("Lists").child(fAuth.getCurrentUser().getUid()).child(listId).child("Items");

        showItems();
    }

    public void createList(View view) {
        // TODO: move to popup on main_activity
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
        DatabaseReference fnewItemDatabase = fItemDatabase.push();

        Map listMap = new HashMap();
        listMap.put("title", "");
        listMap.put("check", false);

        fnewItemDatabase.setValue(listMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showItems();
                }
                else {
                    Toast.makeText( NewListActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showItems()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retreiving items, please wait...");
        progressDialog.show();

        Query query = fItemDatabase.orderByValue();
        FirebaseRecyclerOptions<ItemModel> options = new FirebaseRecyclerOptions.Builder<ItemModel>()
                .setQuery(query, ItemModel.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<ItemModel, ItemViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ItemModel, ItemViewHolder>(options) {

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
                                if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("check"))
                                {
                                    String title = dataSnapshot.child("title").getValue().toString();
                                    String check = dataSnapshot.child("check").getValue().toString();

                                    viewHolder.setItemName(title);
                                    viewHolder.setCheckBox(Boolean.parseBoolean(check));
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(NewListActivity.this, "ERROR: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };
        mItemList.setAdapter(firebaseRecyclerAdapter);
        progressDialog.dismiss();
    }
}
