package com.example.sharinglists;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {

    View mView;

    TextView cardTitle;
    CardView listCard;

    public ListViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

        cardTitle = mView.findViewById(R.id.list_card_title);
        listCard = mView.findViewById(R.id.card_view);
    }

    public void setListTitle(String title) {
        cardTitle.setText(title);
    }
}
