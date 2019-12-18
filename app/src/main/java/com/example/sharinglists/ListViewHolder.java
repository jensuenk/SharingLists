package com.example.sharinglists;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


public class ListViewHolder extends RecyclerView.ViewHolder {

    View mView;

    TextView cardTitle;
    CardView listCard;
    ToggleButton star;

    public ListViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

        cardTitle = mView.findViewById(R.id.list_card_title);
        listCard = mView.findViewById(R.id.card_view);
        star = mView.findViewById(R.id.star_button);
        star.setBackgroundResource(R.drawable.ic_star_unchecked);


    }

    public void setListTitle(String title) {
        cardTitle.setText(title);
    }

    public void setStar(Boolean check) {
        star.setChecked(check);

        if (check)
            star.setBackgroundResource(R.drawable.ic_star_checked);
        else
            star.setBackgroundResource(R.drawable.ic_star_unchecked);
    }
}
