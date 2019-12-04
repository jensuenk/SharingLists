package com.example.sharinglists;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {

    View mView;

    TextView cardTitle;
    TextView cardDescription;

    public ListViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

        cardTitle = mView.findViewById(R.id.list_card_title);
        cardDescription = mView.findViewById(R.id.list_card_description);
    }

    public void setListTitle(String title) {
        cardTitle.setText(title);
    }

    public void setListDescription(String description) {
        cardDescription.setText(description);
    }
}
