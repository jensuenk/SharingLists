package com.example.sharinglists;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private DatabaseReference fListDatabase;

    private RecyclerView mListsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListsList = findViewById(R.id.main_lists_list);
        mListsList.setHasFixedSize(true);
        mListsList.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false));

        fAuth = FirebaseAuth.getInstance();

        updateActivivty();

        if (fAuth != null) {
            fListDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(fAuth.getCurrentUser().getUid());
            generateLists();
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
        Intent newListIntent = new Intent(this, NewListActivity.class);
        startActivity(newListIntent);
        finish();
    }

    public void generateLists() {
        Toast.makeText(this, "Retreiving lists, please wait...", Toast.LENGTH_SHORT).show();

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
                        final String noteId = getRef(position).getKey();
                        fListDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("description"))
                                {
                                    String title = dataSnapshot.child("title").getValue().toString();
                                    String description = dataSnapshot.child("description").getValue().toString();

                                    viewHolder.setListTitle(title);
                                    viewHolder.setListDescription(description);

                                    /*
                                    viewHolder.noteCard.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(EigenLijstActivity.this,NewCocktailActivity.class);
                                            intent.putExtra("noteId",noteId);
                                            startActivity(intent);
                                        }
                                    });

                                     */
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
