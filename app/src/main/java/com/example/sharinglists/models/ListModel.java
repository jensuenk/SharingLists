package com.example.sharinglists.models;

public class ListModel {
    public String listTitle;

    public ListModel() {

    }

    public ListModel(String listTitle) {
        this.listTitle = listTitle;
    }

    public String getListTitle() {
        return listTitle;
    }

    public void setListTitle(String noteTitle) {
        this.listTitle = noteTitle;
    }

}
