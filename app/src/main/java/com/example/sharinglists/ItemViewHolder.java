package com.example.sharinglists;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    View mView;

    TextView itemName;
    CheckBox checkBox;
    CardView listCard;

    public ItemViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

        itemName = mView.findViewById(R.id.item_card_name);
        checkBox = mView.findViewById(R.id.item_card_checkbox);

        listCard = mView.findViewById(R.id.card_view);
    }

    public void setItemName(String name) {
        itemName.setText(name);
    }

    public void setCheckBox(boolean value) {
        checkBox.setChecked(value);
    }
}