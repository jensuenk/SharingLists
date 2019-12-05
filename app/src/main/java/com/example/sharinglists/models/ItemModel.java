package com.example.sharinglists.models;

public class ItemModel {
    public String itemName;
    public boolean checkBox;


    public ItemModel() {

    }

    public ItemModel(String name, boolean value) {
        this.itemName = name;
        this.checkBox = value;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String name) {
        this.itemName = name;
    }

    public boolean getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(boolean value) {
        this.checkBox = value;
    }
}
